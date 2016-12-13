package com.jpmorgan.cakeshop.config;

import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Michael Kazansky
 */
public class SessionListener implements HttpSessionListener {
    
    private static final Logger LOG = LoggerFactory.getLogger(SessionListener.class);
    private final AtomicInteger activeSessions = new AtomicInteger();

    @Override
    public void sessionCreated(HttpSessionEvent event) { 
        activeSessions.incrementAndGet();
        event.getSession().setMaxInactiveInterval(30);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        LOG.info("Decreasing number of sessions " + activeSessions.intValue());
        activeSessions.decrementAndGet();
    }
    
    public Integer getOpenedSesions() {
        return activeSessions.intValue();
    }
    
}
