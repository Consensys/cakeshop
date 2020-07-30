package com.jpmorgan.cakeshop.metrics;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.concurrent.atomic.LongAdder;

public class SimpleRollingAverage {

    private final LongAdder uncounted = new LongAdder();
    private final DescriptiveStatistics stats = new DescriptiveStatistics(12); // 12 vals * 5 sec interval = rolling 1 min of data

    /**
     * Create a new SimpleMovingAverage
     *
     */
    public SimpleRollingAverage() {
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
        stats.addValue(count); // on each tick, add new val to the rolling count
    }

    /**
     * Returns the rate
     *
     * @return the rate
     */
    public double getRate() {
        return stats.getSum();
    }

}
