# Performance Analysis

This page records the current performance position and the bottlenecks addressed before any new benchmark claims are made.

## Fixed Bottlenecks

- Request line and header parsing now reads socket chunks into a reusable per-connection buffer instead of calling `InputStream.read()` for each byte.
- Parser state preserves leftover bytes for keep-alive requests already read from the socket.
- Exact fixed routes are stored in method-specific maps and checked before dynamic routes.
- Dynamic route matching scans path segment indexes and avoids `String.split`.
- Request body string decoding is lazy for `Request.bodyAsString()`.
- Response writing computes `Content-Length`, skips caller-supplied length headers, and writes common status/header bytes without rebuilding a full header string.
- Fast `GET` routes can use prebuilt response bytes without creating `Request`, `Context`, or `Response` objects.
- The primary engine now uses Java 17 NIO with selector-driven read/write readiness.
- Fast path responses are handled on the selector thread.

## Preserved Compatibility

- Main source remains Java 17.
- Main source uses no production dependencies.
- Main source does not directly call JDK 21 virtual-thread APIs.
- The NIO `ServerSocketChannel`/`SocketChannel` engine is the Java 17-compatible path by design.
- Undertow and Vert.x are isolated benchmark references only.

## Remaining Bottlenecks

- Normal handler routes still create `Request`, `Context`, and `Response` objects.
- Request headers are still materialized as strings and maps.
- Request bodies are still fully buffered for `Content-Length`.
- Dynamic route matches still allocate path parameter strings when a dynamic route matches.

## Profiling Commands

JFR:

```bash
PORT=8080 java -Xms512m -Xmx512m \
  -XX:StartFlightRecording=filename=kiss-server.jfr,duration=60s,settings=profile \
  -jar benchmarks/apps/kiss-server-app/target/kiss-server-app.jar
```

Then run:

```bash
BASE_URL=http://127.0.0.1:8080 OUT_DIR=benchmarks/results/profile \
  ./benchmarks/scripts/run-all.sh kiss-server-nio-direct-fast-static
```

## Future Work

Next optimizations should reduce normal-path allocation: lazy or specialized header storage, byte-slice routing for exact paths, cached route response encoders, and lower-allocation write queue nodes.
