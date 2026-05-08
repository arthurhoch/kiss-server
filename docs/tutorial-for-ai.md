---
layout: default
---

# Tutorial For AI Agents And Humans

This tutorial builds a minimal Java 17 Maven application with kiss-server. It is written for AI agents that need to generate correct code and for humans who want to see the intended shape.

## 1. Create A Minimal Maven App

```text
my-kiss-app/
  pom.xml
  src/main/java/com/example/Main.java
```

`pom.xml`:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.example</groupId>
  <artifactId>my-kiss-app</artifactId>
  <version>1.0.0-SNAPSHOT</version>

  <properties>
    <maven.compiler.release>17</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>

  <dependencies>
    <dependency>
      <groupId>io.github.arthurhoch</groupId>
      <artifactId>kiss-server</artifactId>
      <version>0.1.0</version>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>exec-maven-plugin</artifactId>
        <version>3.5.0</version>
      </plugin>
    </plugins>
  </build>
</project>
```

## 2. Create `Main.java`

`src/main/java/com/example/Main.java`:

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
    private static final String VERSION_JSON = "{\"name\":\"my-kiss-app\",\"version\":\"1.0.0\"}";

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

        // Fixed response, so this belongs on the fast path.
        server.fastGet("/health", FastResponses.text("OK"));
        server.fastGet("/version", FastResponses.json(VERSION_JSON));

        // Normal route because the response is application behavior.
        server.get("/hello", ctx -> ctx.text("Hello"));

        // Normal route because it uses a path parameter and can validate input.
        server.get("/users/{id}", ctx -> {
            String id = ctx.pathParam("id");
            if (id == null || !isDigits(id)) {
                return ctx.text(HttpStatus.BAD_REQUEST, "Invalid user id");
            }
            String active = queryValue(ctx.request().queryString(), "active");
            String json = "{\"id\":\"" + escapeJson(id) + "\",\"active\":\"" + escapeJson(active) + "\"}";
            return Response.body(HttpStatus.OK, ContentType.JSON, json, StandardCharsets.UTF_8);
        });

        // Normal route because POST bodies require Request/Context handling.
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
        }, "my-kiss-app-shutdown"));

        System.out.println("listening on http://127.0.0.1:" + handle.port());
        handle.await();
    }

    private static boolean isDigits(String value) {
        if (value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    private static String queryValue(String query, String name) {
        int start = 0;
        while (start <= query.length()) {
            int end = query.indexOf('&', start);
            if (end < 0) {
                end = query.length();
            }
            int equals = query.indexOf('=', start);
            if (equals >= start
                    && equals < end
                    && equals - start == name.length()
                    && query.regionMatches(start, name, 0, name.length())) {
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
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
```

## 3. Why Each Route Was Chosen

| Route | API | Why |
|-------|-----|-----|
| `GET /health` | `fastGet` | fixed health response |
| `GET /version` | `fastGet` | fixed JSON response |
| `GET /hello` | normal `get` | normal application route |
| `GET /users/{id}` | normal `get` | path param, query string, validation, JSON response |
| `POST /echo` | normal `post` | request body handling |

## 4. Run The App

```bash
mvn -q compile exec:java -Dexec.mainClass=com.example.Main
```

## 5. Test With curl

```bash
curl -i http://127.0.0.1:8080/health
curl -i http://127.0.0.1:8080/version
curl -i http://127.0.0.1:8080/hello
curl -i 'http://127.0.0.1:8080/users/42?active=true'
curl -i -X POST http://127.0.0.1:8080/echo \
  -H 'Content-Type: application/json' \
  --data '{"message":"hello"}'
curl -i http://127.0.0.1:8080/users/not-a-number
```

## 6. Benchmark With wrk

Warm up first, then measure:

```bash
wrk --latency -t4 -c100 -d10s http://127.0.0.1:8080/health
wrk --latency -t4 -c100 -d30s http://127.0.0.1:8080/health
wrk --latency -t8 -c500 -d30s http://127.0.0.1:8080/hello
wrk --latency -t8 -c500 -d30s 'http://127.0.0.1:8080/users/42?active=true'
```

Interpret these as local directional results. Keep raw command output with date, JDK, heap, OS, and machine details.

## 7. Optional JDK 21 Virtual-Thread Executor

This is optional application code for JDK 21+. Do not put this call in kiss-server main source and do not show it as required library usage.

```java
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

ServerConfig config = ServerConfig.builder()
        .port(8080)
        .executor(executor)
        .build();
```

Use it when the application has many blocking handlers and after measuring the workload.

## 8. Error Handling Notes

Custom error handler API is not implemented yet. Validate expected app errors in handlers and return explicit responses such as `400` or `413`. Unexpected handler exceptions map to a safe `500 Internal Server Error` response.
