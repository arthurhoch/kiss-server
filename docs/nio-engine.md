---
layout: default
---

# NIO Engine

KissServer uses a Java 17-compatible NIO engine as the primary HTTP/1.1 runtime.

## Runtime Model

- `ServerSocketChannel` accepts connections.
- `SocketChannel` instances run in non-blocking mode.
- A selector thread handles accept, read, parse, and write readiness.
- Each connection owns parser state, a byte input buffer, a write queue, keep-alive counters, timeout state, and close state.
- Fast exact routes are handled on the selector thread with prebuilt response bytes.
- Normal routes run on the configured executor by default.
- Worker responses are queued back to the selector and written with `OP_WRITE`.

## Handler Execution

`HandlerExecutionMode.WORKER` is the default and safe mode. It keeps application handler code off the selector thread.

`HandlerExecutionMode.DIRECT` is advanced benchmark-oriented mode. It runs normal handlers on the selector thread and must not be used for blocking I/O, database calls, file I/O, sleeps, network calls, or slow CPU work.

## Compatibility

The NIO engine uses Java 17 standard library APIs and no production dependencies. Android compatibility remains compatibility-by-design until instrumented Android validation is performed.

## Out of Scope

- HTTP/2
- TLS
- WebSocket
- Servlet API
- external server frameworks
