package io.github.arthurhoch.kiss.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Executor helpers that compile on Java 17.
 */
public final class ExecutorFactories {
    private ExecutorFactories() {
    }

    public static ExecutorService cached(String threadNamePrefix) {
        Objects.requireNonNull(threadNamePrefix, "threadNamePrefix");
        AtomicInteger sequence = new AtomicInteger();
        ThreadFactory factory = runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(threadNamePrefix + "-" + sequence.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
        return Executors.newCachedThreadPool(factory);
    }

    /**
     * Uses virtual threads on JDK 21+ through reflection, otherwise falls back to
     * a cached pool. This helper is optional and not part of the parser hot path.
     */
    public static ExecutorService virtualThreadPerTaskOrCached() {
        try {
            Method method = Executors.class.getMethod("newVirtualThreadPerTaskExecutor");
            Object value = method.invoke(null);
            return (ExecutorService) value;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return cached("kiss-server-worker");
        }
    }
}
