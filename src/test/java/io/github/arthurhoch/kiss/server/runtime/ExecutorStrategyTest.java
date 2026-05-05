package io.github.arthurhoch.kiss.server.runtime;

import io.github.arthurhoch.kiss.server.ServerConfig;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecutorStrategyTest {
    @Test
    void defaultExecutorIsOwnedAndClosedByStrategy() {
        ExecutorStrategy strategy = ExecutorStrategy.from(ServerConfig.defaults());

        assertTrue(strategy.owned());
        assertFalse(strategy.executor().isShutdown());

        strategy.close();

        assertTrue(strategy.executor().isShutdown());
    }

    @Test
    void userExecutorIsNotOwnedOrClosedByStrategy() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            ExecutorStrategy strategy = ExecutorStrategy.from(ServerConfig.builder().executor(executor).build());

            assertFalse(strategy.owned());
            strategy.close();

            assertFalse(executor.isShutdown());
        } finally {
            executor.shutdownNow();
        }
    }
}
