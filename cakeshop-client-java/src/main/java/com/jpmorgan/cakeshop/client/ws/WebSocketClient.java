package com.jpmorgan.cakeshop.client.ws;


import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.messaging.simp.stomp.ConnectionLostException;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SuccessCallback;
import org.springframework.web.socket.client.jetty.JettyWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

public class WebSocketClient {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(WebSocketClient.class);

    private static final long DEFAULT_RECONNECT_DELAY = 1000;

    private final Timer reconnectTimer;

    private final long reconnectDelay;

    private final TaskScheduler taskScheduler;

    private final String wsUri;

    private WebSocketStompClient stompClient;

    private StompSession stompSession;

    private JettyWebSocketClient jettyWebSocketClient;

    private final Map<String, List<EventHandler<?>>> topicHandlers;

    private final List<SuccessCallback<StompSession>> connectListeners;

    private final List<FailureCallback> disconnectListeners;

    private boolean started;

    private boolean shutdown;

    public WebSocketClient(String wsUri) {
        this(wsUri, DEFAULT_RECONNECT_DELAY);
    }

    public WebSocketClient(String wsUri, long reconnectDelay) {
        this.wsUri = wsUri;
        this.taskScheduler = new ThreadPoolTaskScheduler();
        this.reconnectTimer = new Timer("ReconnectTimer");
        this.reconnectDelay = reconnectDelay;
        this.topicHandlers = Collections.synchronizedMap(new LinkedHashMap<String, List<EventHandler<?>>>());
        this.connectListeners = Collections.synchronizedList(new ArrayList<SuccessCallback<StompSession>>());
        this.disconnectListeners = Collections.synchronizedList(new ArrayList<FailureCallback>());
        this.started = false;
        this.shutdown = false;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void subscribe(EventHandler<?> handler) {
        if (topicHandlers.get(handler.getTopic()) == null) {
            topicHandlers.put(handler.getTopic(), Collections.synchronizedList(new ArrayList<EventHandler<?>>()));
        }
        topicHandlers.get(handler.getTopic()).add(handler);
        if (stompSession != null && stompSession.isConnected()) {
            LOG.debug("Subscribing to " + handler.getTopic());
            handler.setStompSubscription(stompSession.subscribe(handler.getTopic(), handler));
        }
    }

    /**
     * Add a listener to fire on successful WebSocket/Stomp connection
     * @param listener
     */
    public void addConnectListener(SuccessCallback<StompSession> listener) {
        connectListeners.add(listener);
    }

    /**
     * Add a listener which fires when the WebSocket/Stomp connection is broken (or fails to connect)
     * @param listener
     */
    public void addDisconnectListener(FailureCallback listener) {
        disconnectListeners.add(listener);
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    private void reconnect() {
        if (this.shutdown) {
            return;
        }
        if (stompClient != null) {
            LOG.debug("Reconnecting");
            disconnect(stompClient);
            createClient();
            doConnect();
            return;
        }
        start();
    }

    public void start() {
        if (this.started) {
            return;
        }
        this.started = true;
        doStart();
    }

    private void doStart() {
        LOG.debug("Connecting");
        createClient();
        doConnect();
    }

    public void shutdown() {
        if (shutdown) {
            return;
        }
        shutdown = true;
        disconnect();
        reconnectTimer.cancel();
    }

    private void disconnect() {
        if (stompClient != null && stompClient.isRunning()) {
            disconnect(stompClient);
            stompClient = null;
            stompSession = null;
        }
    }

    private void disconnect(final WebSocketStompClient stompClient) {
         if (stompClient != null && stompClient.isRunning()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        stompClient.stop();
                    } catch (Throwable t) {
                        LOG.warn("err stopping client: " + t.getMessage());
                    }
                }
            }).start();
        }
    }

    private void createClient() {

        // setup transports & socksjs
        jettyWebSocketClient = new JettyWebSocketClient();
        List<Transport> transports = new ArrayList<>(2);
        transports.add(new WebSocketTransport(jettyWebSocketClient));
        //transports.add(new RestTemplateXhrTransport());

        SockJsClient sockJsClient = new SockJsClient(transports);

        // create stomp client
        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new JsonMessageConverter());
        stompClient.setTaskScheduler(taskScheduler);
        stompClient.start();

    }

    private void doConnect() {

        ListenableFuture<StompSession> future = stompClient.connect(wsUri, new StompSessionHandlerAdapter() {
            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                if (exception instanceof ConnectionLostException) {
                    notifyDisconnectListeners(exception);
                    reconnect(); // reconnect the client
                }
            }
        });

        future.addCallback(
            new SuccessCallback<StompSession>() {
                @Override
                public void onSuccess(StompSession newStompSession) {
                    stompSession = newStompSession;
                    if (topicHandlers != null && !topicHandlers.isEmpty()) {
                        reconnectAllTopics();
                    }
                    notifyConnectListeners(newStompSession);
                }
            },
            new FailureCallback() {
                @Override
                public void onFailure(Throwable throwable) {
                    LOG.debug("Failed to connect: " + throwable.getMessage());
                    notifyDisconnectListeners(throwable);
                    reconnectTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            reconnect();
                        }
                    }, reconnectDelay);
                }
            }
        );

    }

    private void notifyConnectListeners(StompSession session) {
        for (SuccessCallback<StompSession> successCallback : connectListeners) {
            successCallback.onSuccess(session);
        }
    }

    private void notifyDisconnectListeners(Throwable throwable) {
        for (FailureCallback failCallback : disconnectListeners) {
            failCallback.onFailure(throwable);
        }
    }

    private void reconnectAllTopics() {
        if (topicHandlers == null || topicHandlers.isEmpty() || stompSession == null) {
            return;
        }

        pruneInactiveTopicHandlers(); // cleanup handlers before resubscribing

        for (String topic : topicHandlers.keySet()) {
            List<EventHandler<?>> handlers = topicHandlers.get(topic);
            for (EventHandler<?> handler : handlers) {
                if (handler.isActive()) {
                    LOG.debug("Resubscribing to " + topic);
                    handler.setStompSubscription(stompSession.subscribe(topic, handler));
                }
            }
        }
    }

    private void pruneInactiveTopicHandlers() {
        for (String topic : topicHandlers.keySet()) {
             Iterables.removeIf(topicHandlers.get(topic), new Predicate<EventHandler<?>>() {
                @Override
                public boolean apply(EventHandler<?> input) {
                    return !input.isActive();
                }
            });
        }
    }

}
