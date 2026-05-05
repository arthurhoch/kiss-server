package io.github.arthurhoch.kiss.server.protocol.http11;

import io.github.arthurhoch.kiss.server.KissServer;
import io.github.arthurhoch.kiss.server.ServerConfig;
import io.github.arthurhoch.kiss.server.ServerHandle;
import io.github.arthurhoch.kiss.server.routing.FastResponses;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static io.github.arthurhoch.kiss.server.protocol.http11.LoopbackHttpTestClient.connect;
import static io.github.arthurhoch.kiss.server.protocol.http11.LoopbackHttpTestClient.httpClient;
import static io.github.arthurhoch.kiss.server.protocol.http11.LoopbackHttpTestClient.send;

class Http11IntegrationTest {
    @Test
    void handlesGetPostPathParamsQuery404405And500() throws Exception {
        KissServer server = KissServer.create(loopbackConfig())
                .get("/health", ctx -> ctx.text("OK"))
                .post("/echo", ctx -> ctx.text(ctx.bodyAsString()))
                .get("/users/{id}", ctx -> ctx.text(ctx.pathParam("id") + "?" + ctx.request().queryString()))
                .get("/boom", ctx -> {
                    throw new IllegalStateException("secret failure");
                });

        try (ServerHandle handle = server.start()) {
            var client = httpClient();
            assertEquals("OK", send(client, handle, "GET", "/health", "").body());
            assertEquals("hello", send(client, handle, "POST", "/echo", "hello").body());
            assertEquals("42?active=true", send(client, handle, "GET", "/users/42?active=true", "").body());
            assertEquals(404, send(client, handle, "GET", "/missing", "").statusCode());
            assertEquals(405, send(client, handle, "POST", "/health", "").statusCode());
            var failure = send(client, handle, "GET", "/boom", "");
            assertEquals(500, failure.statusCode());
            assertEquals("Internal Server Error", failure.body());
            assertFalse(failure.body().contains("secret failure"));
        }
    }

    @Test
    void supportsKeepAliveAndConnectionCloseOverRawSocket() throws Exception {
        KissServer server = KissServer.create(loopbackConfig())
                .get("/health", ctx -> ctx.text("OK"));

        try (ServerHandle handle = server.start();
             var connection = connect(handle)) {
            connection.write("GET /health HTTP/1.1\r\nHost: localhost\r\n\r\n");
            var first = connection.readResponse();
            assertEquals(200, first.statusCode());
            assertEquals("OK", first.body());
            assertEquals("keep-alive", first.header("Connection"));

            connection.write("GET /health HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n");
            var second = connection.readResponse();
            assertEquals(200, second.statusCode());
            assertEquals("close", second.header("Connection"));
            assertEquals(-1, connection.readByte());
        }
    }

    @Test
    void handlesRawSocketPostEcho() throws Exception {
        KissServer server = KissServer.create(loopbackConfig())
                .post("/echo", ctx -> ctx.text(ctx.bodyAsString()));

        try (ServerHandle handle = server.start();
             var connection = connect(handle)) {
            connection.write("POST /echo HTTP/1.1\r\nHost: localhost\r\nContent-Length: 5\r\nConnection: close\r\n\r\nhello");
            var response = connection.readResponse();
            assertEquals(200, response.statusCode());
            assertEquals("hello", response.body());
            assertEquals("close", response.header("Connection"));
        }
    }

    @Test
    void handlesPartialRequestLineHeadersAndBodyAcrossWrites() throws Exception {
        KissServer server = KissServer.create(loopbackConfig())
                .post("/echo", ctx -> ctx.text(ctx.bodyAsString()));

        try (ServerHandle handle = server.start();
             var connection = connect(handle)) {
            connection.write("POST /ec");
            Thread.sleep(20);
            connection.write("ho HTTP/1.1\r\nHost: local");
            Thread.sleep(20);
            connection.write("host\r\nContent-Length: 5\r\nConnection: close\r\n\r\nhe");
            Thread.sleep(20);
            connection.write("llo");

            var response = connection.readResponse();
            assertEquals(200, response.statusCode());
            assertEquals("hello", response.body());
            assertEquals("close", response.header("Connection"));
        }
    }

    @Test
    void handlesMultipleRequestsInOneSocketWrite() throws Exception {
        KissServer server = KissServer.create(loopbackConfig())
                .get("/health", ctx -> ctx.text("OK"));

        try (ServerHandle handle = server.start();
             var connection = connect(handle)) {
            connection.write("GET /health HTTP/1.1\r\nHost: localhost\r\n\r\n"
                    + "GET /health HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n");

            assertEquals("OK", connection.readResponse().body());
            var second = connection.readResponse();
            assertEquals(200, second.statusCode());
            assertEquals("OK", second.body());
            assertEquals("close", second.header("Connection"));
        }
    }

    @Test
    void closesAfterMaxKeepAliveRequests() throws Exception {
        ServerConfig config = ServerConfig.builder()
                .host("127.0.0.1")
                .port(0)
                .maxKeepAliveRequests(1)
                .build();
        KissServer server = KissServer.create(config).get("/health", ctx -> ctx.text("OK"));

        try (ServerHandle handle = server.start();
             var connection = connect(handle)) {
            connection.write("GET /health HTTP/1.1\r\nHost: localhost\r\n\r\n");
            var response = connection.readResponse();
            assertEquals(200, response.statusCode());
            assertEquals("close", response.header("Connection"));
            assertEquals(-1, connection.readByte());
        }
    }

    @Test
    void rejectsMalformedOversizedHeaderAndOversizedBodySafely() throws Exception {
        ServerConfig config = ServerConfig.builder()
                .host("127.0.0.1")
                .port(0)
                .maxHeaderBytes(48)
                .maxBodyBytes(4)
                .build();
        KissServer server = KissServer.create(config).post("/echo", ctx -> ctx.text(ctx.bodyAsString()));

        try (ServerHandle handle = server.start()) {
            try (var connection = connect(handle)) {
                connection.write("GET / HTTP/1.1\n");
                assertEquals(400, connection.readResponse().statusCode());
            }
            try (var connection = connect(handle)) {
                connection.write("GET / HTTP/1.1\r\nX-Large: 12345678901234567890123456789012345678901234567890\r\n\r\n");
                assertEquals(431, connection.readResponse().statusCode());
            }
            try (var connection = connect(handle)) {
                connection.write("POST /echo HTTP/1.1\r\nContent-Length: 5\r\n\r\nhello");
                assertEquals(413, connection.readResponse().statusCode());
            }
        }
    }

    @Test
    void idleTimeoutClosesKeepAliveConnection() throws Exception {
        ServerConfig config = ServerConfig.builder()
                .host("127.0.0.1")
                .port(0)
                .idleTimeoutMillis(100)
                .readTimeoutMillis(500)
                .build();
        KissServer server = KissServer.create(config).get("/health", ctx -> ctx.text("OK"));

        try (ServerHandle handle = server.start();
             var connection = connect(handle)) {
            connection.write("GET /health HTTP/1.1\r\nHost: localhost\r\n\r\n");
            assertEquals(200, connection.readResponse().statusCode());
            connection.setSoTimeout(1_000);
            assertEquals(-1, connection.readByte());
        }
    }

    @Test
    void fastPathWorksOverSocketAndCanKeepAlive() throws Exception {
        KissServer server = KissServer.create(loopbackConfig())
                .fastGet("/health", FastResponses.text("FAST"));

        try (ServerHandle handle = server.start();
             var connection = connect(handle)) {
            connection.write("GET /health HTTP/1.1\r\nHost: localhost\r\n\r\n");
            var first = connection.readResponse();
            assertEquals("FAST", first.body());
            assertEquals("keep-alive", first.header("Connection"));
            connection.write("GET /health HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n");
            var second = connection.readResponse();
            assertEquals("FAST", second.body());
            assertEquals("close", second.header("Connection"));
        }
    }

    @Test
    void directHandlerExecutionModeRunsNormalHandlerOnSelectorThread() throws Exception {
        java.util.concurrent.atomic.AtomicReference<String> threadName = new java.util.concurrent.atomic.AtomicReference<>();
        ServerConfig config = ServerConfig.builder()
                .host("127.0.0.1")
                .port(0)
                .handlerExecutionMode(io.github.arthurhoch.kiss.server.HandlerExecutionMode.DIRECT)
                .build();
        KissServer server = KissServer.create(config).get("/health", ctx -> {
            threadName.set(Thread.currentThread().getName());
            return ctx.text("OK");
        });

        try (ServerHandle handle = server.start()) {
            assertEquals(200, send(httpClient(), handle, "GET", "/health", "").statusCode());
            assertTrue(threadName.get().startsWith("kiss-server-nio-"));
        }
    }

    @Test
    void fixedNormalRouteHasPriorityOverDynamicRouteAfterFastCheck() throws Exception {
        KissServer server = KissServer.create(loopbackConfig())
                .get("/users/{id}", ctx -> ctx.text("dynamic"))
                .get("/users/me", ctx -> ctx.text("fixed"));

        try (ServerHandle handle = server.start()) {
            assertEquals("fixed", send(httpClient(), handle, "GET", "/users/me", "").body());
        }
    }

    @Test
    void userExecutorIsUsedAndNotShutdownByStop() throws Exception {
        AtomicInteger tasks = new AtomicInteger();
        ExecutorService executor = new AbstractExecutorService() {
            private final ExecutorService delegate = Executors.newSingleThreadExecutor();

            @Override
            public void shutdown() {
                delegate.shutdown();
            }

            @Override
            public java.util.List<Runnable> shutdownNow() {
                return delegate.shutdownNow();
            }

            @Override
            public boolean isShutdown() {
                return delegate.isShutdown();
            }

            @Override
            public boolean isTerminated() {
                return delegate.isTerminated();
            }

            @Override
            public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
                return delegate.awaitTermination(timeout, unit);
            }

            @Override
            public void execute(Runnable command) {
                tasks.incrementAndGet();
                delegate.execute(command);
            }
        };
        ServerConfig config = ServerConfig.builder().host("127.0.0.1").port(0).executor(executor).build();
        KissServer server = KissServer.create(config).get("/health", ctx -> ctx.text("OK"));

        try (ServerHandle handle = server.start()) {
            assertEquals(200, send(httpClient(), handle, "GET", "/health", "").statusCode());
            assertTrue(tasks.get() > 0);
        } finally {
            assertFalse(executor.isShutdown());
            executor.shutdownNow();
        }
    }

    @Test
    void rejectedExecutionClosesSocketWithoutStoppingServer() throws Exception {
        ExecutorService rejecting = new AbstractExecutorService() {
            @Override
            public void shutdown() {
            }

            @Override
            public java.util.List<Runnable> shutdownNow() {
                return java.util.List.of();
            }

            @Override
            public boolean isShutdown() {
                return false;
            }

            @Override
            public boolean isTerminated() {
                return false;
            }

            @Override
            public boolean awaitTermination(long timeout, TimeUnit unit) {
                return true;
            }

            @Override
            public void execute(Runnable command) {
                throw new RejectedExecutionException("rejected");
            }
        };
        ServerConfig config = ServerConfig.builder().host("127.0.0.1").port(0).executor(rejecting).build();

        try (ServerHandle handle = KissServer.create(config).get("/health", ctx -> ctx.text("OK")).start();
             var connection = connect(handle)) {
            connection.write("GET /health HTTP/1.1\r\nHost: localhost\r\n\r\n");
            connection.assertClosedOrReset();
            assertTrue(handle.running());
        }
    }

    @Test
    void handlesSmallConcurrentLoad() throws Exception {
        KissServer server = KissServer.create(loopbackConfig()).get("/health", ctx -> ctx.text("OK"));

        try (ServerHandle handle = server.start()) {
            var client = httpClient(Duration.ofSeconds(2));
            ExecutorService callers = Executors.newFixedThreadPool(20);
            try {
                java.util.List<java.util.concurrent.Future<Integer>> futures = new java.util.ArrayList<>();
                for (int i = 0; i < 100; i++) {
                    futures.add(callers.submit(() -> send(client, handle, "GET", "/health", "").statusCode()));
                }
                for (java.util.concurrent.Future<Integer> future : futures) {
                    assertEquals(200, future.get(5, TimeUnit.SECONDS));
                }
            } finally {
                callers.shutdownNow();
            }
        }
    }

    @Test
    void brokenClientDisconnectDoesNotStopServer() throws Exception {
        KissServer server = KissServer.create(loopbackConfig()).post("/echo", ctx -> ctx.text(ctx.bodyAsString()));

        try (ServerHandle handle = server.start()) {
            try (var connection = connect(handle)) {
                connection.write("POST /echo HTTP/1.1\r\nHost: localhost\r\nContent-Length: 10\r\n\r\nabc");
            }
            assertEquals(404, send(httpClient(), handle, "GET", "/missing", "").statusCode());
            assertTrue(handle.running());
        }
    }

    @Test
    void canStartAndStopMultipleTimes() {
        for (int i = 0; i < 3; i++) {
            try (ServerHandle handle = KissServer.create(loopbackConfig()).get("/health", ctx -> ctx.text("OK")).start()) {
                assertTrue(handle.port() > 0);
                assertTrue(handle.running());
            }
        }
    }

    private static ServerConfig loopbackConfig() {
        return ServerConfig.builder().host("127.0.0.1").port(0).build();
    }
}
