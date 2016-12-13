package com.codahale.metrics;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 */
public class FastMeter {
    private final long TICK_INTERVAL;

    private final SimpleMovingAverage avg = new SimpleMovingAverage(1);

    private final long startTime;
    private final AtomicLong lastTick;
    private final Clock clock;

    private final TickListener tickListener;

    /**
     * Creates a new {@link Meter}.
     */
    public FastMeter(long interval, TickListener tickListener) {
        this(interval, Clock.defaultClock(), tickListener);
    }

    /**
     * Creates a new {@link Meter}.
     *
     * @param clock      the clock to use for the meter ticks
     */
    public FastMeter(long interval, Clock clock, TickListener tickListener) {
        this.TICK_INTERVAL = TimeUnit.SECONDS.toNanos(interval);
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
