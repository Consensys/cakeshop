package com.jpmorgan.cakeshop.config;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.ServletRequestHandledEvent;

/**
 * Log all requests. Not exactly an interceptor, per se, but a listener.
 *
 * @author Chetan Sarva
 *
 */
@Component
public class LoggingInterceptor {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LoggingInterceptor.class);

    @EventListener
    public void onServletRequestEvent(ServletRequestHandledEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(event.getRequestUrl());
        }
    }

}
