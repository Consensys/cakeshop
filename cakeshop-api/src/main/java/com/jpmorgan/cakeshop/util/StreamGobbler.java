package com.jpmorgan.cakeshop.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamGobbler extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(StreamGobbler.class);

    private final InputStream stream;
    private final StringBuilderWriter sw;
    private final Charset encoding;

    public static StreamGobbler create(InputStream stream) {
        StreamGobbler sg = new StreamGobbler(stream);
        sg.start();
        return sg;
    }

    private StreamGobbler(InputStream stream) {
        this.stream = stream;
        this.sw = new StringBuilderWriter();
        this.encoding = Charset.defaultCharset();
    }

    @Override
    public void run() {
        try {
            IOUtils.copy(stream, sw, encoding);
        } catch (IOException e) {
            LOG.warn("Error while gobbling stream: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the current string buffer. May be incomplete at time of calling
     * if Thread is still running.
     *
     * @return
     */
    public String getString() {
        return sw.toString();
    }
}
