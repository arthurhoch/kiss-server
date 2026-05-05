package io.github.arthurhoch.kiss.server.runtime;

import io.github.arthurhoch.kiss.server.ExecutorFactories;
import io.github.arthurhoch.kiss.server.ServerConfig;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

public final class ExecutorStrategy implements AutoCloseable {
    private final ExecutorService executor;
    private final boolean owned;

    private ExecutorStrategy(ExecutorService executor, boolean owned) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.owned = owned;
    }

    public static ExecutorStrategy from(ServerConfig config) {
        Objects.requireNonNull(config, "config");
        if (config.executor() != null) {
            return new ExecutorStrategy(config.executor(), false);
        }
        return new ExecutorStrategy(ExecutorFactories.cached("kiss-server-worker"), true);
    }

    public ExecutorService executor() {
        return executor;
    }

    public boolean owned() {
        return owned;
    }

    @Override
    public void close() {
        if (owned) {
            executor.shutdown();
        }
    }
}
