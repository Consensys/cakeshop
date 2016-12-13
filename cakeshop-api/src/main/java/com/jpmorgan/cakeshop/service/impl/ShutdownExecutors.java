package com.jpmorgan.cakeshop.service.impl;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 *
 * @author Michael Kazansky
 */
@Component
public class ShutdownExecutors  implements DisposableBean {

    @Autowired(required = false)
    @Qualifier("asyncTaskExecutor")
    private ThreadPoolTaskExecutor asyncTaskExecutor;


    @Override
    public void destroy() throws Exception {
        asyncTaskExecutor.getThreadPoolExecutor().shutdownNow();
    }
    
}
