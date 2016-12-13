package com.jpmorgan.cakeshop.client.ws;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpmorgan.cakeshop.client.model.res.APIData;
import com.jpmorgan.cakeshop.client.model.res.APIResponse;

import java.io.IOException;
import java.lang.reflect.Type;

import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession.Subscription;

public abstract class EventHandler<T> implements StompFrameHandler {

    protected static final ObjectMapper objectMapper = new ObjectMapper();

    private Subscription stompSubscription;

    private boolean active = true;

    /**
     * Get the Topic name this handler should attach to
     *
     * @return
     */
    public abstract String getTopic();

    /**
     * Jackson value type that is expected to be returned
     *
     * @return
     */
    public abstract JavaType getValType();

    /**
     * Listener method which fires when new data arrives
     *
     * @param data
     */
    public abstract void onData(T data);

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return String.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        try {
            APIResponse<APIData<T>, T> val = objectMapper.readValue((String) payload, getValType());
            onData(val.getData());

        } catch (IOException e) {
            throw new RuntimeException("Failed to decode message", e);
        }
    }

    public void unsubscribe() {
        this.active = false;
        if (getStompSubscription() != null) {
            getStompSubscription().unsubscribe();
        }
    }

    public Subscription getStompSubscription() {
        return stompSubscription;
    }

    public void setStompSubscription(Subscription stompSubscription) {
        this.stompSubscription = stompSubscription;
    }

    public boolean isActive() {
        return active;
    }

}
