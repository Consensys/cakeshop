package com.jpmorgan.cakeshop.util;

import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ProcessUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessUtils.class);

    public static ProcessBuilder createProcessBuilder(List<String> commands) {
        ProcessBuilder builder = new ProcessBuilder(commands);

        if (LOG.isDebugEnabled()) {
            LOG.debug(Joiner.on(" ").join(builder.command()));
        }

        return builder;
    }

}
