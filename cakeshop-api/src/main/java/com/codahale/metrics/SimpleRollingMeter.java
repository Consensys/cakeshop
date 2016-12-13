package com.codahale.metrics;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class SimpleRollingMeter {

    private final long TICK_INTERVAL = TimeUnit.SECONDS.toNanos(5);

    private final SimpleRollingAverage avg = new SimpleRollingAverage();

    private final long startTime;
    private final AtomicLong lastTick;
    private final Clock clock;

    private final TickListener tickListener;

    public SimpleRollingMeter() {
        this(null);
    }

    public SimpleRollingMeter(TickListener tickListener) {
        this(Clock.defaultClock(), tickListener);
    }

    public SimpleRollingMeter(Clock clock, TickListener tickListener) {
        this.clock = clock;
        this.startTime = this.clock.getTick();
        this.lastTick = new AtomicLong(startTime);
        this.tickListener = tickListener;
    }

    /**
     * Mark the occurrence of an event.
     */
    public void mark() {
        mark(1);
    }

    /**
     * Mark the occurrence of a given number of events.
     *
     * @param n the number of events
     */
    public void mark(long n) {
        tickIfNecessary();
        avg.update(n);
    }

    private void tickIfNecessary() {
        final long oldTick = lastTick.get();
        final long newTick = clock.getTick();
        final long age = newTick - oldTick;
        if (age > TICK_INTERVAL) {
            final long newIntervalStartTick = newTick - age % TICK_INTERVAL;
            if (lastTick.compareAndSet(oldTick, newIntervalStartTick)) {
                final long requiredTicks = age / TICK_INTERVAL;
                for (long i = 0; i < requiredTicks; i++) {
                    avg.tick();
                    if (tickListener != null) {
                        tickListener.nextTick(avg.getRate());
                    }
                }
            }
        }
    }

    public double getRate() {
        return avg.getRate();
    }

}
