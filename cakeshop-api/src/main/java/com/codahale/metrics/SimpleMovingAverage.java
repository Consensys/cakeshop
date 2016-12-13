package com.codahale.metrics;

public class SimpleMovingAverage {

    private volatile double rate = 0.0;

    private final LongAdder uncounted = new LongAdder();
    private final double interval;

    /**
     * Create a new SimpleMovingAverage
     *
     * @param interval     the expected tick interval, in seconds
     */
    public SimpleMovingAverage(long interval) {
        this.interval = interval;
    }

    /**
     * Update the moving average with a new value.
     *
     * @param n the new value
     */
    public void update(long n) {
        uncounted.add(n);
    }

    /**
     * Mark the passage of time and decay the current rate accordingly.
     */
    public void tick() {
        final long count = uncounted.sumThenReset();
        rate = count / interval;
    }

    /**
     * Returns the rate
     *
     * @return the rate
     */
    public double getRate() {
        return rate;
    }

}
