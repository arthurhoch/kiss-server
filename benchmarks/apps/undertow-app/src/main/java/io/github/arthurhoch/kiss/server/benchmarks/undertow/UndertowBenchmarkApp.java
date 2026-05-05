package io.github.arthurhoch.kiss.server.benchmarks.undertow;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;

import java.util.Deque;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

public final class UndertowBenchmarkApp {
    private static final String JSON = "{\"message\":\"hello\",\"value\":123}";
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String TEXT_CONTENT_TYPE = "text/plain";

    private UndertowBenchmarkApp() {
    }

    public static void main(String[] args) throws InterruptedException {
        String host = setting("HOST", "0.0.0.0");
        int port = Integer.parseInt(setting("PORT", "8080"));
        Undertow server = Undertow.builder()
                .addHttpListener(port, host)
                .setHandler(new BenchmarkHandler())
                .build();

        CountDownLatch shutdown = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
            shutdown.countDown();
        }, "undertow-benchmark-shutdown"));
        server.start();
        System.out.println("undertow benchmark app listening on " + host + ":" + port);
        shutdown.await();
    }

    private static final class BenchmarkHandler implements HttpHandler {
        @Override
        public void handleRequest(HttpServerExchange exchange) {
            String path = exchange.getRequestPath();
            if (Methods.GET.equals(exchange.getRequestMethod())) {
                handleGet(exchange, path);
                return;
            }
            if (Methods.POST.equals(exchange.getRequestMethod())) {
                handlePost(exchange, path);
                return;
            }
            exchange.setStatusCode(StatusCodes.NOT_FOUND);
            exchange.endExchange();
        }

        private static void handleGet(HttpServerExchange exchange, String path) {
            if ("/health".equals(path)) {
                sendText(exchange, "OK");
                return;
            }
            if ("/hello".equals(path)) {
                sendText(exchange, "Hello");
                return;
            }
            if ("/json".equals(path)) {
                sendJson(exchange, JSON);
                return;
            }
            String id = userId(path);
            if (id != null) {
                sendJson(exchange, userJson(id, firstQueryValue(exchange, "active")));
                return;
            }
            exchange.setStatusCode(StatusCodes.NOT_FOUND);
            exchange.endExchange();
        }

        private static void handlePost(HttpServerExchange exchange, String path) {
            if ("/echo".equals(path)) {
                exchange.getRequestReceiver().receiveFullString(
                        (received, body) -> sendJson(received, body),
                        (received, error) -> sendError(received)
                );
                return;
            }
            if ("/consume".equals(path)) {
                exchange.getRequestReceiver().receiveFullBytes(
                        (received, body) -> {
                            received.setStatusCode(StatusCodes.NO_CONTENT);
                            received.endExchange();
                        },
                        (received, error) -> sendError(received)
                );
                return;
            }
            exchange.setStatusCode(StatusCodes.NOT_FOUND);
            exchange.endExchange();
        }

        private static void sendText(HttpServerExchange exchange, String body) {
            exchange.setStatusCode(StatusCodes.OK);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, TEXT_CONTENT_TYPE);
            exchange.getResponseSender().send(body);
        }

        private static void sendJson(HttpServerExchange exchange, String body) {
            exchange.setStatusCode(StatusCodes.OK);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, JSON_CONTENT_TYPE);
            exchange.getResponseSender().send(body);
        }

        private static void sendError(HttpServerExchange exchange) {
            exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
            exchange.getResponseSender().send("Internal Server Error");
        }
    }

    private static String userId(String path) {
        String prefix = "/users/";
        if (!path.startsWith(prefix) || path.length() == prefix.length()) {
            return null;
        }
        String id = path.substring(prefix.length());
        return id.indexOf('/') < 0 ? id : null;
    }

    private static String firstQueryValue(HttpServerExchange exchange, String name) {
        Deque<String> values = exchange.getQueryParameters().get(name);
        return values == null || values.isEmpty() ? "" : values.peekFirst();
    }

    private static String userJson(String id, String active) {
        return "{\"id\":\"" + escapeJson(id) + "\",\"active\":\"" + escapeJson(active) + "\"}";
    }

    private static String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
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
