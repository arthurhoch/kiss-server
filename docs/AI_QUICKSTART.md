---
layout: default
---

# AI Quickstart

Paste this into AI context when generating a kiss-server app.

## Dependency

```xml
<dependency>
  <groupId>io.github.arthurhoch</groupId>
  <artifactId>kiss-server</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Minimal Java 17 App

```java
import io.github.arthurhoch.kiss.server.KissServer;
import io.github.arthurhoch.kiss.server.ServerConfig;
import io.github.arthurhoch.kiss.server.http.HttpStatus;
import io.github.arthurhoch.kiss.server.routing.FastResponses;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Main {
    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(8);

        ServerConfig config = ServerConfig.builder()
                .port(8080)
                .executor(executor)
                .build();

        KissServer server = KissServer.create(config);

        server.fastGet("/health", FastResponses.text("OK"));

        server.get("/users/{id}", ctx -> ctx.text("User " + ctx.pathParam("id")));

        server.post("/echo", ctx -> {
            if (ctx.request().body().length > 64 * 1024) {
                return ctx.text(HttpStatus.PAYLOAD_TOO_LARGE, "Payload too large");
            }
            return ctx.text(ctx.bodyAsString());
        });

        server.start(8080).await();
    }
}
```

## Performance Rules

- Fixed exact `GET` response: use `fastGet` + `FastResponses`.
- Dynamic or business route: use normal `get`/`post`.
- POST body: avoid `bodyAsString()` unless text is needed.
- Production app: configure `ExecutorService` explicitly.
- Blocking work: keep it off fast path and off `DIRECT` mode.
- Java 17 by default. JDK 21 virtual threads are optional application code only.

## Do

- Keep handlers small.
- Set content type explicitly for JSON.
- Validate domain body limits.
- Reuse static response strings/bytes.
- Benchmark with warmup, p99, and raw results.

## Don't

- Do not add Spring, Netty, Vert.x, Undertow, or Servlet just to use kiss-server.
- Do not add a JSON dependency unless requested.
- Do not use fast path for dynamic logic.
- Do not claim Android or Native Image support is validated until tests exist.
- Do not mix fast-static and normal-route benchmark claims.
