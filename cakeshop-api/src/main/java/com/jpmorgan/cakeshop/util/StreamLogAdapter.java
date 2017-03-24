package com.jpmorgan.cakeshop.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;

public class StreamLogAdapter extends InterruptibleExecutionThreadService {

    private final Logger logger;

    private final BufferedReader reader;

    public StreamLogAdapter(Logger logger, InputStream stream) {
        super();
        this.logger = logger;
        this.reader = new BufferedReader(new InputStreamReader(stream));
    }

    @Override
    protected void run() throws Exception {
        while (isRunning()) {
            String line = reader.readLine();
            if (line == null) {
                return;
            }
            logger.debug(line);
        }
    }

    @Override
    protected void shutDown() throws Exception {
        super.shutDown();
        interruptRunningThread();
    }

}
