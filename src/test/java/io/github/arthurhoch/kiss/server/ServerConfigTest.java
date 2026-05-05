package io.github.arthurhoch.kiss.server;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerConfigTest {
    @Test
    void defaultsAreSafe() {
        ServerConfig config = ServerConfig.defaults();

        assertEquals("0.0.0.0", config.host());
        assertEquals(8080, config.port());
        assertNull(config.executor());
        assertEquals(10_000, config.maxConnections());
        assertEquals(16 * 1024, config.maxHeaderBytes());
        assertEquals(8 * 1024, config.maxRequestLineBytes());
        assertEquals(10L * 1024 * 1024, config.maxBodyBytes());
        assertEquals(5_000, config.readTimeoutMillis());
        assertEquals(10_000, config.writeTimeoutMillis());
        assertEquals(30_000, config.idleTimeoutMillis());
        assertTrue(config.keepAlive());
        assertEquals(1_000, config.maxKeepAliveRequests());
        assertEquals(16 * 1024, config.bufferSize());
        assertEquals(2_048, config.bufferPoolSize());
        assertEquals(HandlerExecutionMode.WORKER, config.handlerExecutionMode());
    }

    @Test
    void builderSetsValues() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            ServerConfig config = ServerConfig.builder()
                    .host("127.0.0.1")
                    .port(9090)
                    .executor(executor)
                    .maxConnections(100)
                    .maxHeaderBytes(1024)
                    .maxRequestLineBytes(512)
                    .maxBodyBytes(2048)
                    .readTimeoutMillis(1000)
                    .writeTimeoutMillis(2000)
                    .idleTimeoutMillis(3000)
                    .keepAlive(false)
                    .maxKeepAliveRequests(10)
                    .bufferSize(4096)
                    .bufferPoolSize(8)
                    .handlerExecutionMode(HandlerExecutionMode.DIRECT)
                    .build();

            assertEquals("127.0.0.1", config.host());
            assertEquals(9090, config.port());
            assertSame(executor, config.executor());
            assertEquals(100, config.maxConnections());
            assertEquals(1024, config.maxHeaderBytes());
            assertEquals(512, config.maxRequestLineBytes());
            assertEquals(2048, config.maxBodyBytes());
            assertEquals(1000, config.readTimeoutMillis());
            assertEquals(2000, config.writeTimeoutMillis());
            assertEquals(3000, config.idleTimeoutMillis());
            assertFalse(config.keepAlive());
            assertEquals(10, config.maxKeepAliveRequests());
            assertEquals(4096, config.bufferSize());
            assertEquals(8, config.bufferPoolSize());
            assertEquals(HandlerExecutionMode.DIRECT, config.handlerExecutionMode());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void rejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> ServerConfig.builder().host(" ").build());
        assertThrows(NullPointerException.class, () -> ServerConfig.builder().host(null).build());
        assertThrows(IllegalArgumentException.class, () -> ServerConfig.builder().port(-1).build());
        assertThrows(IllegalArgumentException.class, () -> ServerConfig.builder().port(65_536).build());
        assertThrows(IllegalArgumentException.class, () -> ServerConfig.builder().maxConnections(0).build());
        assertThrows(IllegalArgumentException.class, () -> ServerConfig.builder().maxHeaderBytes(0).build());
        assertThrows(IllegalArgumentException.class, () -> ServerConfig.builder().maxRequestLineBytes(0).build());
        assertThrows(IllegalArgumentException.class, () -> ServerConfig.builder().maxBodyBytes(0).build());
        assertThrows(IllegalArgumentException.class, () -> ServerConfig.builder().readTimeoutMillis(0).build());
        assertThrows(IllegalArgumentException.class, () -> ServerConfig.builder().writeTimeoutMillis(0).build());
        assertThrows(IllegalArgumentException.class, () -> ServerConfig.builder().idleTimeoutMillis(0).build());
        assertThrows(IllegalArgumentException.class, () -> ServerConfig.builder().maxKeepAliveRequests(0).build());
        assertThrows(IllegalArgumentException.class, () -> ServerConfig.builder().bufferSize(0).build());
        assertThrows(IllegalArgumentException.class, () -> ServerConfig.builder().bufferPoolSize(0).build());
        assertThrows(NullPointerException.class, () -> ServerConfig.builder().handlerExecutionMode(null).build());
    }

    @Test
    void withPortReturnsNewConfigAndKeepsOtherValues() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            ServerConfig original = ServerConfig.builder()
                    .host("127.0.0.1")
                    .port(8080)
                    .executor(executor)
                    .keepAlive(false)
                    .build();

            ServerConfig changed = original.withPort(0);

            assertEquals(8080, original.port());
            assertEquals(0, changed.port());
            assertEquals(original.host(), changed.host());
            assertSame(executor, changed.executor());
            assertFalse(changed.keepAlive());
            assertEquals(original.handlerExecutionMode(), changed.handlerExecutionMode());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void executorFactoryCreatesExecutor() {
        ExecutorService executor = ExecutorFactories.cached("test-kiss-server");
        try {
            assertNotNull(executor);
        } finally {
            executor.shutdownNow();
        }
    }
}
