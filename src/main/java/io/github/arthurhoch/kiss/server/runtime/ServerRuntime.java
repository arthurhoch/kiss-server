package io.github.arthurhoch.kiss.server.runtime;

import io.github.arthurhoch.kiss.server.ServerHandle;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ServerRuntime implements ServerHandle {
    private final int port;
    private final Runnable stopAction;
    private final CountDownLatch stopped = new CountDownLatch(1);
    private final AtomicBoolean running = new AtomicBoolean(true);

    public ServerRuntime(int port, Runnable stopAction) {
        this.port = port;
        this.stopAction = stopAction == null ? () -> { } : stopAction;
    }

    @Override
    public int port() {
        return port;
    }

    @Override
    public boolean running() {
        return running.get();
    }

    @Override
    public void await() throws InterruptedException {
        stopped.await();
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            try {
                stopAction.run();
            } finally {
                stopped.countDown();
            }
        }
    }
}
