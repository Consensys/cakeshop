package com.jpmorgan.cakeshop.util;

import com.jpmorgan.cakeshop.service.WebSocketPushService;
import com.jpmorgan.cakeshop.service.impl.WebSocketPushServiceImpl;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StreamLogAdapter extends InterruptibleExecutionThreadService {

    @Autowired
    private WebSocketPushService websocket;

    private Logger logger;

    private BufferedReader reader;

    public StreamLogAdapter(Logger logger, InputStream stream) {
        super();
        this.logger = logger;
        this.reader = new BufferedReader(new InputStreamReader(stream));
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void setReader(InputStream stream) {
        this.reader = new BufferedReader(new InputStreamReader(stream));
    }

    public StreamLogAdapter() {

    }

    @Override
    protected void run() throws Exception {
        while (isRunning()) {
            String line = reader.readLine();
            if (line == null) {
                return;
            }
            logger.debug(line);
            websocket.pushGethLogs(line);
        }
    }

    @Override
    protected void shutDown() throws Exception {
        super.shutDown();
        interruptRunningThread();
    }

}
