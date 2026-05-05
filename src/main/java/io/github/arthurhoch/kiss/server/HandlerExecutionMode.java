package io.github.arthurhoch.kiss.server;

/**
 * Controls where normal route handlers run in the NIO engine.
 */
public enum HandlerExecutionMode {
    /**
     * Safe default: normal handlers run on the configured executor.
     */
    WORKER,

    /**
     * Advanced mode: normal handlers run on the selector thread.
     *
     * <p>Use only for handlers that never block, sleep, perform file I/O, call
     * databases, or perform slow work.</p>
     */
    DIRECT
}
