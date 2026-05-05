package io.github.arthurhoch.kiss.server.runtime;

import java.util.concurrent.atomic.AtomicInteger;

public final class ConnectionLimiter {
    private final int maxConnections;
    private final AtomicInteger active = new AtomicInteger();

    public ConnectionLimiter(int maxConnections) {
        if (maxConnections <= 0) {
            throw new IllegalArgumentException("maxConnections must be positive");
        }
        this.maxConnections = maxConnections;
    }

    public boolean tryAcquire() {
        while (true) {
            int current = active.get();
            if (current >= maxConnections) {
                return false;
            }
            if (active.compareAndSet(current, current + 1)) {
                return true;
            }
        }
    }

    public void release() {
        while (true) {
            int current = active.get();
            if (current <= 0) {
                throw new IllegalStateException("no active connection to release");
            }
            if (active.compareAndSet(current, current - 1)) {
                return;
            }
        }
    }

    public int availablePermits() {
        return maxConnections - active.get();
    }

    public int activeCount() {
        return active.get();
    }
}
