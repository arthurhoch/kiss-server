package io.github.arthurhoch.kiss.server;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * Immutable server configuration.
 *
 * <p>If an {@link ExecutorService} is provided, the application owns its
 * shutdown. If no executor is provided, KissServer creates and owns one.</p>
 */
public record ServerConfig(
        String host,
        int port,
        ExecutorService executor,
        int maxConnections,
        int maxHeaderBytes,
        int maxRequestLineBytes,
        long maxBodyBytes,
        int readTimeoutMillis,
        int writeTimeoutMillis,
        int idleTimeoutMillis,
        boolean keepAlive,
        int maxKeepAliveRequests,
        int bufferSize,
        int bufferPoolSize,
        HandlerExecutionMode handlerExecutionMode
) {
    public static final String DEFAULT_HOST = "0.0.0.0";
    public static final int DEFAULT_PORT = 8080;
    public static final int DEFAULT_MAX_CONNECTIONS = 10_000;
    public static final int DEFAULT_MAX_HEADER_BYTES = 16 * 1024;
    public static final int DEFAULT_MAX_REQUEST_LINE_BYTES = 8 * 1024;
    public static final long DEFAULT_MAX_BODY_BYTES = 10L * 1024 * 1024;
    public static final int DEFAULT_READ_TIMEOUT_MILLIS = 5_000;
    public static final int DEFAULT_WRITE_TIMEOUT_MILLIS = 10_000;
    public static final int DEFAULT_IDLE_TIMEOUT_MILLIS = 30_000;
    public static final boolean DEFAULT_KEEP_ALIVE = true;
    public static final int DEFAULT_MAX_KEEP_ALIVE_REQUESTS = 1_000;
    public static final int DEFAULT_BUFFER_SIZE = 16 * 1024;
    public static final int DEFAULT_BUFFER_POOL_SIZE = 2_048;
    public static final HandlerExecutionMode DEFAULT_HANDLER_EXECUTION_MODE = HandlerExecutionMode.WORKER;

    public ServerConfig(
            String host,
            int port,
            ExecutorService executor,
            int maxConnections,
            int maxHeaderBytes,
            int maxRequestLineBytes,
            long maxBodyBytes,
            int readTimeoutMillis,
            int writeTimeoutMillis,
            int idleTimeoutMillis,
            boolean keepAlive,
            int maxKeepAliveRequests,
            int bufferSize,
            int bufferPoolSize
    ) {
        this(host, port, executor, maxConnections, maxHeaderBytes, maxRequestLineBytes, maxBodyBytes,
                readTimeoutMillis, writeTimeoutMillis, idleTimeoutMillis, keepAlive, maxKeepAliveRequests,
                bufferSize, bufferPoolSize, DEFAULT_HANDLER_EXECUTION_MODE);
    }

    public ServerConfig {
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(handlerExecutionMode, "handlerExecutionMode");
        if (host.isBlank()) {
            throw new IllegalArgumentException("host must not be blank");
        }
        requirePort(port);
        requirePositive(maxConnections, "maxConnections");
        requirePositive(maxHeaderBytes, "maxHeaderBytes");
        requirePositive(maxRequestLineBytes, "maxRequestLineBytes");
        requirePositive(maxBodyBytes, "maxBodyBytes");
        requirePositive(readTimeoutMillis, "readTimeoutMillis");
        requirePositive(writeTimeoutMillis, "writeTimeoutMillis");
        requirePositive(idleTimeoutMillis, "idleTimeoutMillis");
        requirePositive(maxKeepAliveRequests, "maxKeepAliveRequests");
        requirePositive(bufferSize, "bufferSize");
        requirePositive(bufferPoolSize, "bufferPoolSize");
    }

    /**
     * Returns the default configuration.
     *
     * @return default server configuration
     */
    public static ServerConfig defaults() {
        return builder().build();
    }

    /**
     * Creates a builder initialized with default values.
     *
     * @return configuration builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns a copy of this configuration with a different port.
     *
     * @param newPort port to bind, or {@code 0} for an operating-system assigned port
     * @return copied configuration
     */
    public ServerConfig withPort(int newPort) {
        return new ServerConfig(
                host,
                newPort,
                executor,
                maxConnections,
                maxHeaderBytes,
                maxRequestLineBytes,
                maxBodyBytes,
                readTimeoutMillis,
                writeTimeoutMillis,
                idleTimeoutMillis,
                keepAlive,
                maxKeepAliveRequests,
                bufferSize,
                bufferPoolSize,
                handlerExecutionMode
        );
    }

    private static void requirePort(int value) {
        if (value < 0 || value > 65_535) {
            throw new IllegalArgumentException("port must be between 0 and 65535");
        }
    }

    private static void requirePositive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }

    private static void requirePositive(long value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }

    public static final class Builder {
        private String host = DEFAULT_HOST;
        private int port = DEFAULT_PORT;
        private ExecutorService executor;
        private int maxConnections = DEFAULT_MAX_CONNECTIONS;
        private int maxHeaderBytes = DEFAULT_MAX_HEADER_BYTES;
        private int maxRequestLineBytes = DEFAULT_MAX_REQUEST_LINE_BYTES;
        private long maxBodyBytes = DEFAULT_MAX_BODY_BYTES;
        private int readTimeoutMillis = DEFAULT_READ_TIMEOUT_MILLIS;
        private int writeTimeoutMillis = DEFAULT_WRITE_TIMEOUT_MILLIS;
        private int idleTimeoutMillis = DEFAULT_IDLE_TIMEOUT_MILLIS;
        private boolean keepAlive = DEFAULT_KEEP_ALIVE;
        private int maxKeepAliveRequests = DEFAULT_MAX_KEEP_ALIVE_REQUESTS;
        private int bufferSize = DEFAULT_BUFFER_SIZE;
        private int bufferPoolSize = DEFAULT_BUFFER_POOL_SIZE;
        private HandlerExecutionMode handlerExecutionMode = DEFAULT_HANDLER_EXECUTION_MODE;

        private Builder() {
        }

        /**
         * Sets the bind host.
         */
        public Builder host(String host) {
            this.host = host;
            return this;
        }

        /**
         * Sets the bind port, or {@code 0} for an operating-system assigned port.
         */
        public Builder port(int port) {
            this.port = port;
            return this;
        }

        /**
         * Sets the executor for normal route handlers.
         *
         * <p>The application owns shutdown of a provided executor.</p>
         */
        public Builder executor(ExecutorService executor) {
            this.executor = executor;
            return this;
        }

        public Builder maxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        public Builder maxHeaderBytes(int maxHeaderBytes) {
            this.maxHeaderBytes = maxHeaderBytes;
            return this;
        }

        public Builder maxRequestLineBytes(int maxRequestLineBytes) {
            this.maxRequestLineBytes = maxRequestLineBytes;
            return this;
        }

        public Builder maxBodyBytes(long maxBodyBytes) {
            this.maxBodyBytes = maxBodyBytes;
            return this;
        }

        public Builder readTimeoutMillis(int readTimeoutMillis) {
            this.readTimeoutMillis = readTimeoutMillis;
            return this;
        }

        public Builder writeTimeoutMillis(int writeTimeoutMillis) {
            this.writeTimeoutMillis = writeTimeoutMillis;
            return this;
        }

        public Builder idleTimeoutMillis(int idleTimeoutMillis) {
            this.idleTimeoutMillis = idleTimeoutMillis;
            return this;
        }

        public Builder keepAlive(boolean keepAlive) {
            this.keepAlive = keepAlive;
            return this;
        }

        public Builder maxKeepAliveRequests(int maxKeepAliveRequests) {
            this.maxKeepAliveRequests = maxKeepAliveRequests;
            return this;
        }

        public Builder bufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }

        public Builder bufferPoolSize(int bufferPoolSize) {
            this.bufferPoolSize = bufferPoolSize;
            return this;
        }

        public Builder handlerExecutionMode(HandlerExecutionMode handlerExecutionMode) {
            this.handlerExecutionMode = handlerExecutionMode;
            return this;
        }

        public ServerConfig build() {
            return new ServerConfig(
                    host,
                    port,
                    executor,
                    maxConnections,
                    maxHeaderBytes,
                    maxRequestLineBytes,
                    maxBodyBytes,
                    readTimeoutMillis,
                    writeTimeoutMillis,
                    idleTimeoutMillis,
                    keepAlive,
                    maxKeepAliveRequests,
                    bufferSize,
                    bufferPoolSize,
                    handlerExecutionMode
            );
        }
    }
}
