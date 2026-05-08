---
layout: default
---

# Performance

KissServer should be fast because it is small and direct, not because it hides complexity behind a framework runtime.

## Current Architecture Summary

- Java 17-compatible NIO HTTP/1.1 engine.
- Selector-driven accept, read, parse, and write readiness.
- Bounded parser buffers and explicit request limits.
- Exact route lookup before dynamic route matching.
- Normal route handlers run on the configured `ExecutorService` by default.
- Fast static exact `GET` routes can avoid normal `Request`, `Context`, and `Response` allocation.
- Response writer computes `Content-Length` and writes common status/header bytes efficiently.
- Zero production dependencies.

## NIO Engine

The primary runtime uses `ServerSocketChannel`, `SocketChannel`, `Selector`, and `ByteBuffer`. The selector thread owns network I/O and connection state. Normal handler work is dispatched to the configured executor unless `HandlerExecutionMode.DIRECT` is explicitly selected.

`DIRECT` mode is advanced and must not be used for blocking handlers.

## Fast-Static Mode

Fast-static mode is for exact fixed `GET` endpoints:

```java
server.fastGet("/health", FastResponses.text("OK"));
```

It exists for endpoints such as `/health`, `/ready`, `/version`, `/ping`, and `/robots.txt` when the response is fixed. It is faster because the response bytes are prebuilt and the engine can avoid normal route object creation.

Fast-static benchmark results must be labeled separately.

## Normal Mode

Normal mode is the default for business routes:

```java
server.get("/users/{id}", ctx -> ctx.text(ctx.pathParam("id")));
server.post("/echo", ctx -> ctx.text(ctx.bodyAsString()));
```

Normal mode supports path params, headers, query string access, request body handling, validation, response helpers, and application logic. It still has more allocation and executor handoff cost than fast-static routes.

## Latest Local Benchmark Snapshot

Environment:

- Java: Temurin 21.0.11
- wrk: 4.2.0 [kqueue]
- Heap: `-Xms512m -Xmx512m`
- Host: localhost
- Warmup: 10s
- Measured: 30s
- wrk option: `--latency`
- Raw results: `benchmarks/results/20260504T211305Z-nio-rerun`

These are local directional results. They are useful for development and comparison under this setup, but they are not universal production guarantees.

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

Undertow and Vert.x are benchmark references only. They are not production dependencies of kiss-server.

## Bottlenecks Remaining

- Dynamic route path param matching still allocates path parameter strings.
- Normal routes still create `Request`, `Context`, and `Response` objects.
- Request headers are materialized as strings and maps.
- POST bodies are fully buffered for `Content-Length`.
- Worker-mode normal routes pay selector/worker handoff cost.
- p99 latency still needs attention under high concurrency.

## Future Optimization Targets

- dynamic route path param allocation;
- normal route `Request`/`Context`/`Response` allocation;
- POST body handling;
- p99 latency;
- selector/worker handoff cost.

Any future performance claim must include raw results, environment details, route mode labels, and a clear distinction between fast-static and normal-route workloads.
