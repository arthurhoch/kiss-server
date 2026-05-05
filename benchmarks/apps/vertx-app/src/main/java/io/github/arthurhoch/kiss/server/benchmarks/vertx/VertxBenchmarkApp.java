package io.github.arthurhoch.kiss.server.benchmarks.vertx;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class VertxBenchmarkApp {
    private static final String JSON = "{\"message\":\"hello\",\"value\":123}";
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String TEXT_CONTENT_TYPE = "text/plain";

    private VertxBenchmarkApp() {
    }

    public static void main(String[] args) throws InterruptedException {
        String host = setting("HOST", "0.0.0.0");
        int port = Integer.parseInt(setting("PORT", "8080"));
        Vertx vertx = Vertx.vertx();
        HttpServer server = vertx.createHttpServer();
        CountDownLatch started = new CountDownLatch(1);
        AtomicReference<Throwable> startupFailure = new AtomicReference<>();

        server.requestHandler(VertxBenchmarkApp::handleRequest)
                .listen(port, host)
                .onSuccess(httpServer -> started.countDown())
                .onFailure(error -> {
                    startupFailure.set(error);
                    started.countDown();
                });
        if (!started.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Timed out waiting for Vert.x server start");
        }
        if (startupFailure.get() != null) {
            throw new IllegalStateException("Could not start Vert.x benchmark app", startupFailure.get());
        }

        CountDownLatch shutdown = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            vertx.close();
            shutdown.countDown();
        }, "vertx-benchmark-shutdown"));
        System.out.println("vertx benchmark app listening on " + host + ":" + port);
        shutdown.await();
    }

    private static void handleRequest(HttpServerRequest request) {
        String path = request.path();
        if (request.method() == HttpMethod.GET) {
            handleGet(request, path);
            return;
        }
        if (request.method() == HttpMethod.POST) {
            handlePost(request, path);
            return;
        }
        request.response().setStatusCode(404).end();
    }

    private static void handleGet(HttpServerRequest request, String path) {
        if ("/health".equals(path)) {
            sendText(request.response(), "OK");
            return;
        }
        if ("/hello".equals(path)) {
            sendText(request.response(), "Hello");
            return;
        }
        if ("/json".equals(path)) {
            sendJson(request.response(), JSON);
            return;
        }
        String id = userId(path);
        if (id != null) {
            sendJson(request.response(), userJson(id, valueOrEmpty(request.getParam("active"))));
            return;
        }
        request.response().setStatusCode(404).end();
    }

    private static void handlePost(HttpServerRequest request, String path) {
        if ("/echo".equals(path)) {
            request.body()
                    .onSuccess(body -> sendJson(request.response(), body.toString(StandardCharsets.UTF_8.name())))
                    .onFailure(error -> sendError(request.response()));
            return;
        }
        if ("/consume".equals(path)) {
            request.body()
                    .onSuccess(body -> request.response().setStatusCode(204).end())
                    .onFailure(error -> sendError(request.response()));
            return;
        }
        request.response().setStatusCode(404).end();
    }

    private static void sendText(HttpServerResponse response, String body) {
        response.setStatusCode(200)
                .putHeader("Content-Type", TEXT_CONTENT_TYPE)
                .end(body);
    }

    private static void sendJson(HttpServerResponse response, String body) {
        response.setStatusCode(200)
                .putHeader("Content-Type", JSON_CONTENT_TYPE)
                .end(body);
    }

    private static void sendError(HttpServerResponse response) {
        response.setStatusCode(500).end("Internal Server Error");
    }

    private static String userId(String path) {
        String prefix = "/users/";
        if (!path.startsWith(prefix) || path.length() == prefix.length()) {
            return null;
        }
        String id = path.substring(prefix.length());
        return id.indexOf('/') < 0 ? id : null;
    }

    private static String userJson(String id, String active) {
        return "{\"id\":\"" + escapeJson(id) + "\",\"active\":\"" + escapeJson(active) + "\"}";
    }

    private static String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String setting(String name, String fallback) {
        String property = System.getProperty(name.toLowerCase(Locale.ROOT));
        if (property != null && !property.isBlank()) {
            return property;
        }
        String environment = System.getenv(name);
        return environment == null || environment.isBlank() ? fallback : environment;
    }
}
