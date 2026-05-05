# KissServer

A tiny, zero-production-dependency Java 17+ HTTP/1.1 server library for simple APIs, local tools, MVPs, internal services, embedded use cases, and AI-generated applications.

KissServer exists for cases where a small, explicit Java HTTP server is enough and a framework runtime would be more than the job needs. It uses a Java 17-compatible NIO HTTP/1.1 engine, a small public API, bounded parser limits, and an application-provided or server-owned `ExecutorService` for normal route handlers.

Status: initial HTTP/1.1 core is ready for the first `0.1.0` release.

KissServer is a sibling of [kiss-requests](https://github.com/arthurhoch/kiss-requests) and [kiss-json](https://github.com/arthurhoch/kiss-json). It does not depend on either project.

## Highlights

- Zero production dependencies.
- Java 17+ main artifact.
- NIO-based HTTP/1.1 socket engine.
- Simple `get`, `post`, dynamic route, and response APIs.
- Fast path for exact static/simple `GET` responses.
- Normal path for business routes.
- Configurable `ExecutorService` for handler execution.
- Optional JDK 21 virtual-thread executor usage from application code.
- Android-compatible by design, pending real device or emulator validation.
- GraalVM Native Image friendly by design, pending validated example or CI support.

## Maven

```xml
<dependency>
    <groupId>io.github.arthurhoch</groupId>
    <artifactId>kiss-server</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Hello World

```java
import io.github.arthurhoch.kiss.server.KissServer;

public final class Main {
    public static void main(String[] args) throws Exception {
        KissServer server = KissServer.create();
        server.get("/health", ctx -> ctx.text("OK"));
        server.start(8080).await();
    }
}
```

## Common Routes

GET route:

```java
server.get("/hello", ctx -> ctx.text("Hello"));
```

POST route:

```java
server.post("/echo", ctx -> ctx.text(ctx.bodyAsString()));
```

Dynamic route:

```java
server.get("/users/{id}", ctx -> {
    String id = ctx.pathParam("id");
    return ctx.text("User " + id);
});
```

Query params are currently exposed as the raw query string:

```java
server.get("/search", ctx -> {
    String query = ctx.request().queryString();
    return ctx.text("query=" + query);
});
```

Request and response headers:

```java
server.get("/headers", ctx -> {
    String agent = ctx.request().header("User-Agent");
    return ctx.text("agent=" + (agent == null ? "" : agent))
            .header("X-App", "kiss-server");
});
```

Custom status:

```java
import io.github.arthurhoch.kiss.server.http.HttpStatus;

server.get("/missing", ctx -> ctx.text(HttpStatus.NOT_FOUND, "Missing"));
```

JSON without a required JSON dependency:

```java
import io.github.arthurhoch.kiss.server.http.ContentType;
import io.github.arthurhoch.kiss.server.http.HttpStatus;
import io.github.arthurhoch.kiss.server.http.Response;

server.get("/json", ctx -> {
    String json = "{\"message\":\"hello\"}";
    return Response.body(HttpStatus.OK, ContentType.JSON, json, java.nio.charset.StandardCharsets.UTF_8);
});
```

Prebuilt static JSON bytes for an exact fast route:

```java
import io.github.arthurhoch.kiss.server.routing.FastResponses;

server.fastGet("/version", FastResponses.json("{\"version\":\"0.1.0\"}"));
```

## Fast Path And Normal Path

Use the fast path only when the response is fixed and the route is exact:

```java
server.fastGet("/health", FastResponses.text("OK"));
```

Good fast path candidates include `/health`, `/ready`, `/version`, `/ping`, and `/robots.txt` when the response is static.

Use normal routes for business logic, dynamic path params, query-dependent responses, request bodies, database calls, file calls, network calls, and anything that needs `Request`, `Context`, or `Response` flexibility.

## Executor Configuration

Normal handlers run on the configured `ExecutorService` by default. If no executor is provided, KissServer creates and owns one. If an executor is provided, the application owns its shutdown.

```java
import io.github.arthurhoch.kiss.server.KissServer;
import io.github.arthurhoch.kiss.server.ServerConfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

ExecutorService executor = Executors.newFixedThreadPool(
        Math.max(4, Runtime.getRuntime().availableProcessors())
);

ServerConfig config = ServerConfig.builder()
        .host("0.0.0.0")
        .port(8080)
        .executor(executor)
        .maxConnections(10_000)
        .maxHeaderBytes(16 * 1024)
        .maxRequestLineBytes(8 * 1024)
        .maxBodyBytes(10L * 1024 * 1024)
        .build();

KissServer server = KissServer.create(config);
```

Optional JDK 21 application code may pass a virtual-thread executor:

```java
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

ServerConfig config = ServerConfig.builder()
        .port(8080)
        .executor(executor)
        .build();
```

KissServer main source remains Java 17-compatible and does not call JDK 21-only APIs directly.

## Performance Notes

KissServer is designed to be fast because it is small and direct: byte-oriented parsing, bounded buffers, selector-based I/O, exact route checks before dynamic routes, precomputed response writer pieces, a fast path for static exact `GET` responses, and no framework runtime.

The normal path is still the right default for application behavior. Fast-static results must be labeled separately because they avoid normal `Request`, `Context`, and `Response` allocation.

Undertow and Vert.x appear in benchmark modules as reference servers only. They are not dependencies of the main library.

## Latest Local Benchmark Snapshot

Environment:

- Java: Temurin 21.0.11
- wrk: 4.2.0 [kqueue]
- Heap: `-Xms512m -Xmx512m`
- Host: localhost
- Warmup: 10s
- Measured: 30s
- wrk option: `--latency`
- Raw results: [benchmarks/results/20260504T211305Z-nio-rerun](benchmarks/results/20260504T211305Z-nio-rerun)

These are local directional results, not universal production guarantees. Real systems include TLS, proxies, containers, CPU limits, network devices, payload variance, and different JVM or OS settings.

| Scenario | Best kiss-server | Undertow | Vert.x | Winner |
|----------|------------------|----------|--------|--------|
| `health_t4_c100` | 209,089 req/s, p99 1.12ms | 157,380 req/s, p99 0.86ms | 157,356 req/s, p99 1.75ms | kiss-server |
| `health_t8_c500` | 162,441 req/s, p99 5.62ms | 150,673 req/s, p99 3.87ms | 145,309 req/s, p99 5.25ms | kiss-server |
| `hello_t8_c500` | 158,021 req/s, p99 6.05ms | 149,329 req/s, p99 4.03ms | 144,778 req/s, p99 6.77ms | kiss-server |
| `json_t8_c500` | 156,804 req/s, p99 4.55ms | 149,090 req/s, p99 4.04ms | 147,406 req/s, p99 5.56ms | kiss-server |
| `users_t8_c500` | 144,125 req/s, p99 4.99ms | 145,921 req/s, p99 4.06ms | 156,504 req/s, p99 5.14ms | Vert.x |
| `post_echo_t8_c500` | 137,829 req/s, p99 5.30ms | 136,610 req/s, p99 4.99ms | 147,247 req/s, p99 4.97ms | Vert.x |
| `post_consume_t8_c500` | 144,844 req/s, p99 4.79ms | 143,158 req/s, p99 4.18ms | 150,992 req/s, p99 4.92ms | Vert.x |

Fairness note: The winning kiss-server simple endpoint results use the clearly labeled fast-static mode with prebuilt responses for `/health`, `/hello`, and `/json`. Dynamic and POST workloads are benchmarked separately and still have different bottlenecks.

## Deployment Recommendation

Production deployments should usually terminate TLS and HTTP/2 or HTTP/3 at Cloudflare, Nginx, Caddy, or a similar edge/proxy layer, then forward local HTTP/1.1 traffic to KissServer:

```text
client/browser
  -> Cloudflare/Nginx/Caddy using TLS + HTTP/2 or HTTP/3
      -> kiss-server using HTTP/1.1 locally
```

For local tools, tests, internal services, and embedded uses, KissServer can bind directly to loopback or a configured interface.

## Intentionally Not Supported Yet

- HTTP/2 in the core.
- TLS implementation in the core.
- WebSocket in the core.
- Servlet API.
- Spring, Quarkus, Netty, Jetty, Undertow, Vert.x, or other framework/server dependency in the main artifact.
- Built-in JSON dependency.
- Annotation scanning or dependency injection container.
- Reflection in the hot path.

## Documentation

- [Documentation Index](docs/index.md)
- [GitHub Pages](https://arthurhoch.github.io/kiss-server)
- [Getting Started](docs/getting-started.md)
- [API](docs/api.md)
- [Examples](docs/examples.md)
- [Using kiss-server with AI coding agents](docs/ai-usage.md)
- [Tutorial for AI agents and humans](docs/tutorial-for-ai.md)
- [AI Performance Guide](docs/ai-performance-guide.md)
- [AI Quickstart](docs/AI_QUICKSTART.md)
- [Fast Path](docs/fast-path.md)
- [Executor Model](docs/executor-model.md)
- [Performance](docs/performance.md)
- [Benchmarking](docs/benchmarking.md)
- [Android](docs/android.md)
- [Native Image](docs/native-image.md)
- [Deployment](docs/deployment.md)
- [Architecture](.github/architecture/index.md)

## Maven Central Status

Release configuration is present. Publishing requires Sonatype Central Portal setup, GPG signing setup, and GitHub repository secrets.

## Build

```bash
mvn -B verify
```

## License

Apache License 2.0. Copyright 2026 Arthur Hoch. See [LICENSE.txt](LICENSE.txt).
