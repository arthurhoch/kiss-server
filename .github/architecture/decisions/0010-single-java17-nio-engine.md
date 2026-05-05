# ADR 0010: Single Java 17 NIO Engine

## Status

Accepted.

## Context

The blocking socket engine was correct and simple, but local benchmarks showed that one worker thread per active connection was a major throughput and tail-latency bottleneck.

KissServer still needs Java 17 source compatibility, zero production dependencies, no JDK 21 requirement, and Android compatibility by design.

## Decision

Use a Java 17-compatible NIO selector engine as the primary HTTP/1.1 runtime.

The engine uses:

- `ServerSocketChannel`
- `SocketChannel`
- `Selector`
- `SelectionKey`
- `ByteBuffer`
- `ExecutorService`

Fast exact routes run on the selector thread. Normal handlers run on the configured executor by default. Advanced direct handler execution is explicit and documented as unsafe for blocking handlers.

## Consequences

- The main artifact remains Java 17-compatible and dependency-free.
- JDK 21 virtual threads remain application or benchmark configuration only.
- Android compatibility remains by design, pending actual instrumented validation.
- The old blocking implementation may remain in source temporarily during migration, but the public runtime path is NIO.
- HTTP/2, TLS, WebSocket, Servlet, and external server frameworks remain out of scope.
