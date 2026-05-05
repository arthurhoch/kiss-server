package io.github.arthurhoch.kiss.server.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectionLimiterTest {
    @Test
    void tracksActiveConnectionsAndLimitsCapacity() {
        ConnectionLimiter limiter = new ConnectionLimiter(2);

        assertTrue(limiter.tryAcquire());
        assertTrue(limiter.tryAcquire());
        assertFalse(limiter.tryAcquire());
        assertEquals(2, limiter.activeCount());
        assertEquals(0, limiter.availablePermits());

        limiter.release();
        assertEquals(1, limiter.activeCount());
        assertEquals(1, limiter.availablePermits());
        assertTrue(limiter.tryAcquire());
    }

    @Test
    void rejectsInvalidReleaseAndInvalidMax() {
        assertThrows(IllegalArgumentException.class, () -> new ConnectionLimiter(0));
        ConnectionLimiter limiter = new ConnectionLimiter(1);
        assertThrows(IllegalStateException.class, limiter::release);
    }
}
