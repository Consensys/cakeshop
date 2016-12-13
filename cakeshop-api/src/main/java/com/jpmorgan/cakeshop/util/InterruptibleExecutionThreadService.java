package com.jpmorgan.cakeshop.util;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public abstract class InterruptibleExecutionThreadService extends AbstractExecutionThreadService {

    private final AtomicReference<Thread> runningThread = new AtomicReference<Thread>(null);

    @Override
    protected Executor executor() {
        return new Executor() {
            @Override
            public void execute(Runnable command) {
                Thread thread = Executors.defaultThreadFactory().newThread(command);
                runningThread.compareAndSet(null, thread);

                try {
                    thread.setName(serviceName());
                } catch (SecurityException e) {
                    // OK if we can't set the name in this environment.
                }
                thread.start();
            }
        };
    }

    protected void interruptRunningThread() {
        Thread thread = runningThread.get();
        if (thread != null) {
            thread.interrupt();
        }
    }

}
