# Example AI-Generated App

This is the shape an AI agent should generate for a small Java 17 kiss-server application.

## Complete `Main.java`

```java
package com.example;

import io.github.arthurhoch.kiss.server.KissServer;
import io.github.arthurhoch.kiss.server.ServerConfig;
import io.github.arthurhoch.kiss.server.ServerHandle;
import io.github.arthurhoch.kiss.server.http.ContentType;
import io.github.arthurhoch.kiss.server.http.HttpStatus;
import io.github.arthurhoch.kiss.server.http.Response;
import io.github.arthurhoch.kiss.server.routing.FastResponses;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Main {
    private static final String VERSION_JSON = "{\"name\":\"example\",\"version\":\"1.0.0\"}";

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(
                Math.max(4, Runtime.getRuntime().availableProcessors())
        );

        ServerConfig config = ServerConfig.builder()
                .host("0.0.0.0")
                .port(8080)
                .executor(executor)
                .maxBodyBytes(1024 * 1024)
                .build();

        KissServer server = KissServer.create(config);

        // Fixed responses use the fast path.
        server.fastGet("/health", FastResponses.text("OK"));
        server.fastGet("/version", FastResponses.json(VERSION_JSON));

        // Business and dynamic routes use the normal path.
        server.get("/hello", ctx -> ctx.text("Hello"));

        server.get("/users/{id}", ctx -> {
            String id = ctx.pathParam("id");
            if (id == null || id.isBlank()) {
                return ctx.text(HttpStatus.BAD_REQUEST, "Missing id");
            }
            String json = "{\"id\":\"" + escapeJson(id) + "\"}";
            return Response.body(HttpStatus.OK, ContentType.JSON, json, StandardCharsets.UTF_8);
        });

        server.post("/echo", ctx -> {
            byte[] body = ctx.request().body();
            if (body.length > 64 * 1024) {
                return ctx.text(HttpStatus.PAYLOAD_TOO_LARGE, "Payload too large");
            }
            return Response.body(HttpStatus.OK, ContentType.JSON, ctx.bodyAsString(), StandardCharsets.UTF_8);
        });

        ServerHandle handle = server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            handle.stop();
            executor.shutdown();
        }, "example-shutdown"));

        System.out.println("listening on http://127.0.0.1:" + handle.port());
        handle.await();
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
```

## Curl Commands

```bash
curl -i http://127.0.0.1:8080/health
curl -i http://127.0.0.1:8080/version
curl -i http://127.0.0.1:8080/hello
curl -i http://127.0.0.1:8080/users/42
curl -i -X POST http://127.0.0.1:8080/echo \
  -H 'Content-Type: application/json' \
  --data '{"message":"hello"}'
```

## Benchmark Commands

```bash
wrk --latency -t4 -c100 -d10s http://127.0.0.1:8080/health
wrk --latency -t4 -c100 -d30s http://127.0.0.1:8080/health
wrk --latency -t8 -c500 -d30s http://127.0.0.1:8080/hello
wrk --latency -t8 -c500 -d30s http://127.0.0.1:8080/users/42
```

Label `/health` and `/version` as fast path results. Label `/hello`, `/users/{id}`, and `/echo` as normal route results.
