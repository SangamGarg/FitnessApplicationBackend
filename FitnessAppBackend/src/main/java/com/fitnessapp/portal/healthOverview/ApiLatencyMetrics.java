package com.fitnessapp.portal.healthOverview;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ApiLatencyMetrics {
    private final LongAdder requestCount = new LongAdder();
    private final LongAdder totalLatencyNanos = new LongAdder();
    private final AtomicLong maximumLatencyNanos = new AtomicLong();

    public void record(long elapsedNanos) {
        requestCount.increment();
        totalLatencyNanos.add(elapsedNanos);
        maximumLatencyNanos.accumulateAndGet(elapsedNanos, Math::max);
    }

    public long requestCount() {
        return requestCount.sum();
    }

    public double averageLatencyMs() {
        long count = requestCount.sum();
        return count == 0 ? 0 : totalLatencyNanos.sum() / (double) count / 1_000_000d;
    }

    public long maximumLatencyMs() {
        return maximumLatencyNanos.get() / 1_000_000L;
    }
}
