# AI Performance Guide

This guide focuses only on generating and tuning performant kiss-server applications.

## Performance Model

KissServer uses a Java 17-compatible NIO HTTP/1.1 engine. The selector handles network I/O. Normal route handlers run on the configured `ExecutorService` by default. Exact static fast path routes can be served without creating normal `Request`, `Context`, or `Response` objects.

Performance depends on route shape, handler work, executor choice, allocation rate, blocking behavior, payload size, JVM, OS, and deployment topology.

## Fast Path Vs Normal Path

Fast path:

- exact `GET` route;
- fixed prebuilt response bytes;
- no `Context`;
- no request-dependent logic;
- intended for health, readiness, version, and similar static endpoints.

Normal path:

- regular `get`, `post`, and other method routes;
- supports path params, headers, query string access, request body, response helpers, validation, and business logic;
- safe default for application behavior.

## Route Decision Table

| Use case | Recommended API | Reason |
|----------|-----------------|--------|
| `/health` fixed response | `fastGet` + `FastResponses.text` | exact fixed health response |
| `/ready` fixed response | `fastGet` + `FastResponses.text` | exact fixed readiness response |
| `/version` fixed JSON | `fastGet` + `FastResponses.json` | prebuilt static JSON bytes |
| `/users/{id}` | `get` normal route | needs path parameter and app logic |
| `POST /echo` | `post` normal route | needs request body |
| `POST JSON business command` | `post` normal route | needs validation and domain behavior |
| file serving | not core / future / custom handler | static file server is not a core feature yet |
| WebSocket | not supported | outside current scope |
| TLS | put Nginx/Caddy/Cloudflare in front | core serves HTTP/1.1 |

## Static Responses

Use prebuilt fast responses for fixed endpoints:

```java
server.fastGet("/health", FastResponses.text("OK"));
server.fastGet("/version", FastResponses.json("{\"version\":\"1.0.0\"}"));
```

Reuse static strings or bytes. Do not rebuild static JSON on every request.

## Dynamic Responses

Use normal routes:

```java
server.get("/users/{id}", ctx -> {
    String id = ctx.pathParam("id");
    String json = "{\"id\":\"" + escapeJson(id) + "\"}";
    return Response.body(HttpStatus.OK, ContentType.JSON, json, StandardCharsets.UTF_8);
});
```

Keep hot handlers small. Avoid large temporary lists, maps, and string builders unless they are needed.

## Request Body Handling

`ctx.request().body()` returns a defensive copy of the body bytes. `ctx.bodyAsString()` decodes UTF-8 lazily and caches that string for the request.

Performance rules:

- use bytes when bytes are enough;
- call `bodyAsString()` once if text is needed;
- enforce domain-specific body limits when lower than `ServerConfig.maxBodyBytes`;
- avoid parsing large JSON manually in multiple routes.

## Header, Query, And Path Param Access

Path params:

```java
String id = ctx.pathParam("id");
```

Headers:

```java
String contentType = ctx.request().header("Content-Type");
```

Query string:

```java
String query = ctx.request().queryString();
```

There is no parsed query-param API yet. Parse only what the route needs and keep parsing local and bounded.

## Executor Selection

`WORKER` mode is the safe default. Normal handlers run on the configured executor.

Use `DIRECT` only when every normal handler is non-blocking, short, and predictable. Do not use `DIRECT` for database calls, file I/O, network calls, sleeps, or slow CPU work.

## JDK 17 Executor Example

```java
ExecutorService executor = Executors.newFixedThreadPool(
        Math.max(4, Runtime.getRuntime().availableProcessors())
);

ServerConfig config = ServerConfig.builder()
        .port(8080)
        .executor(executor)
        .build();
```

For workloads with blocking calls, a cached pool may be more appropriate, but measure queueing, CPU, and memory under load.

## JDK 21 Virtual-Thread Executor Example

Optional JDK 21 application code:

```java
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

ServerConfig config = ServerConfig.builder()
        .port(8080)
        .executor(executor)
        .build();
```

Label benchmark results clearly as `kiss-server on JDK 21 with virtual-thread executor`.

## Avoiding Allocation

- Put fixed responses on the fast path.
- Reuse static strings for static JSON.
- Avoid building large strings in hot routes.
- Avoid per-request maps/lists unless needed.
- Avoid duplicate body conversion.
- Keep dynamic route path params minimal.
- Prefer small helper methods over generic abstraction layers that allocate.

## Avoiding Blocking Bottlenecks

- Do not block the selector thread.
- Do not use `DIRECT` mode for blocking handlers.
- Use an executor sized for the handler workload.
- Avoid synchronized global state in hot routes.
- Avoid synchronous per-request logging in benchmarks and hot production paths.
- Time out downstream database, file, and network work in application code.

## Benchmarking Generated Apps

Use `wrk` with warmup and measured runs:

```bash
wrk --latency -t4 -c100 -d10s http://127.0.0.1:8080/health
wrk --latency -t4 -c100 -d30s http://127.0.0.1:8080/health
wrk --latency -t8 -c500 -d30s http://127.0.0.1:8080/users/123
```

Record:

- date and commit;
- JDK distribution and version;
- JVM flags and heap;
- OS and machine;
- command;
- warmup duration;
- measured duration;
- raw output;
- p50, p90, p99, requests/sec, and errors.

## Reading wrk Results

`Requests/sec` shows throughput for that local run. p99 latency shows the latency below which 99 percent of observed requests completed. A higher requests/sec value with worse p99 may not be better for a latency-sensitive service.

Watch for:

- socket errors;
- non-2xx/3xx responses;
- high p99 or max latency;
- throughput drops between warmup and measured runs;
- CPU saturation;
- GC spikes;
- server logs that indicate rejected execution or connection resets.

## Performance Checklist

- Fixed endpoints use `fastGet`.
- Dynamic and POST endpoints use normal routes.
- No blocking work in fast path or selector/direct mode.
- Executor is explicit for production examples.
- Body conversion happens only when needed.
- Static JSON is prebuilt.
- Hot routes avoid global locks and large temporary objects.
- Benchmarks include warmup, p99, raw output, and environment.
- Fast-static numbers are labeled separately from normal-route numbers.
- No universal performance claims are made from one local benchmark.
