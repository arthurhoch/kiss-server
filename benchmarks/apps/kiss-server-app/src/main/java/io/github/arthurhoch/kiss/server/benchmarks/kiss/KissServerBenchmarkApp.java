package io.github.arthurhoch.kiss.server.benchmarks.kiss;

import io.github.arthurhoch.kiss.server.KissServer;
import io.github.arthurhoch.kiss.server.HandlerExecutionMode;
import io.github.arthurhoch.kiss.server.ServerConfig;
import io.github.arthurhoch.kiss.server.ServerHandle;
import io.github.arthurhoch.kiss.server.http.HttpStatus;
import io.github.arthurhoch.kiss.server.http.Response;
import io.github.arthurhoch.kiss.server.routing.FastResponses;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class KissServerBenchmarkApp {
    private static final String JSON = "{\"message\":\"hello\",\"value\":123}";
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String MODE_WORKER = "worker";
    private static final String MODE_DIRECT = "direct";
    private static final String MODE_VIRTUAL_THREADS = "virtual-threads";

    private KissServerBenchmarkApp() {
    }

    public static void main(String[] args) throws InterruptedException {
        String host = setting("HOST", "0.0.0.0");
        int port = Integer.parseInt(setting("PORT", "8080"));
        String mode = setting("KISS_MODE", setting("KISS_EXECUTOR", MODE_WORKER));
        boolean fastStatic = Boolean.parseBoolean(setting("KISS_FAST_STATIC", "true"));
        ExecutorService executor = null;
        ServerConfig.Builder config = ServerConfig.builder().host(host).port(port);
        String label = "kiss-server-nio-worker";
        if (MODE_VIRTUAL_THREADS.equals(mode)) {
            executor = Executors.newVirtualThreadPerTaskExecutor();
            config.executor(executor);
            label = "kiss-server-nio-virtual-threads-jdk21";
        } else if (MODE_DIRECT.equals(mode)) {
            config.handlerExecutionMode(HandlerExecutionMode.DIRECT);
            label = "kiss-server-nio-direct";
        } else if (!MODE_WORKER.equals(mode)) {
            throw new IllegalArgumentException("KISS_MODE must be worker, direct, or virtual-threads");
        }

        KissServer server = KissServer.create(config.build())
                .get("/users/{id}", ctx -> json(userJson(ctx.pathParam("id"), queryValue(ctx.request().queryString(), "active"))))
                .post("/echo", ctx -> json(ctx.bodyAsString()))
                .post("/consume", ctx -> Response.status(HttpStatus.NO_CONTENT));
        if (fastStatic) {
            label = label + "-fast-static";
            server.fastGet("/health", FastResponses.text("OK"));
            server.fastGet("/hello", FastResponses.text("Hello"));
            server.fastGet("/json", FastResponses.json(JSON));
        } else {
            server.get("/health", ctx -> text("OK"));
            server.get("/hello", ctx -> text("Hello"));
            server.get("/json", ctx -> json(JSON));
        }

        ServerHandle handle = server.start();
        CountDownLatch shutdown = new CountDownLatch(1);
        ExecutorService ownedExecutor = executor;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            handle.stop();
            if (ownedExecutor != null) {
                ownedExecutor.shutdown();
            }
            shutdown.countDown();
        }, "kiss-benchmark-shutdown"));
        System.out.println(label + " benchmark app listening on " + host + ":" + handle.port());
        if (MODE_VIRTUAL_THREADS.equals(mode)) {
            System.out.println("label: kiss-server on JDK 21 with virtual-thread executor");
        }
        if (fastStatic) {
            System.out.println("label: exact static GET endpoints use kiss-server fast path");
        }
        shutdown.await();
    }

    private static Response text(String body) {
        return Response.body(HttpStatus.OK, "text/plain", body, StandardCharsets.UTF_8);
    }

    private static Response json(String body) {
        return Response.body(HttpStatus.OK, JSON_CONTENT_TYPE, body, StandardCharsets.UTF_8);
    }

    private static String userJson(String id, String active) {
        return "{\"id\":\"" + escapeJson(id) + "\",\"active\":\"" + escapeJson(active) + "\"}";
    }

    private static String queryValue(String query, String name) {
        int start = 0;
        while (start <= query.length()) {
            int end = query.indexOf('&', start);
            if (end < 0) {
                end = query.length();
            }
            int equals = query.indexOf('=', start);
            if (equals - start == name.length() && query.regionMatches(start, name, 0, name.length())) {
                return query.substring(equals + 1, end);
            }
            if (end == query.length()) {
                return "";
            }
            start = end + 1;
        }
        return "";
    }

    private static String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String setting(String name, String fallback) {
        String property = System.getProperty(name.toLowerCase(java.util.Locale.ROOT));
        if (property != null && !property.isBlank()) {
            return property;
        }
        String environment = System.getenv(name);
        return environment == null || environment.isBlank() ? fallback : environment;
    }
}
