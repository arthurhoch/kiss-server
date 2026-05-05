package io.github.arthurhoch.kiss.server;

/**
 * Handle returned by a started server.
 *
 * <p>Use it to discover the bound port, wait for shutdown, or stop the server.
 * Closing the handle stops the server.</p>
 */
public interface ServerHandle extends AutoCloseable {
    /**
     * Returns the bound port. This is useful when the configured port was {@code 0}.
     *
     * @return bound port
     */
    int port();

    /**
     * Returns whether the server is still running.
     *
     * @return {@code true} while the server is running
     */
    boolean running();

    /**
     * Blocks until the server stops.
     *
     * @throws InterruptedException if interrupted while waiting
     */
    void await() throws InterruptedException;

    /**
     * Requests server shutdown.
     */
    void stop();

    /**
     * Stops the server.
     */
    @Override
    default void close() {
        stop();
    }
}
