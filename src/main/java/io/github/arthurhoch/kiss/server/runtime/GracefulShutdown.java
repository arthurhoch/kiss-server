package io.github.arthurhoch.kiss.server.runtime;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public final class GracefulShutdown {
    private GracefulShutdown() {
    }

    public static void shutdown(ExecutorService executor, long timeoutMillis) {
        if (executor == null) {
            return;
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(timeoutMillis, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }
}
