# NIO Engine

The primary engine is a Java 17-compatible NIO HTTP/1.1 engine.

## Selector Loop

- `OP_ACCEPT` accepts new `SocketChannel` connections.
- `OP_READ` reads available bytes into per-connection buffers.
- The parser scans bytes by index and can extract multiple complete requests from one read.
- Fast exact routes can be handled immediately on the selector thread with prebuilt response bytes.
- Normal handlers run on the configured executor unless direct mode is explicitly configured.
- Worker responses are queued back to the connection and `selector.wakeup()` is used.
- `OP_WRITE` is enabled only while output is pending.

## Connection State

Each connection tracks:

- `SocketChannel`;
- `SelectionKey`;
- byte input buffer and parser indexes;
- parser body state;
- pending `ByteBuffer` writes;
- pending write byte count;
- keep-alive request count;
- last activity time;
- close-after-write flag;
- handler-in-progress flag.

## Normal And Fast Paths

Normal path creates `Request`, `Context`, invokes `Handler`, and writes a `Response`. This is the default application path.

Fast path serves exact static `GET` responses from prebuilt bytes. It exists for fixed endpoints and must be reported separately in benchmarks.

## Direct Mode

Direct mode is advanced and unsafe for blocking handlers. It is useful for controlled benchmarks and short CPU-only handlers, but it must not be the default and must not be used for database, file, network, sleep, or slow work.

## Compatibility

The engine uses Java 17 standard NIO APIs, no production dependencies, no reflection, no service loading, no JNI, and no JDK-internal APIs.

Android compatibility remains compatibility-by-design until instrumented Android validation is performed.
