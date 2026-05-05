# Socket Engine

The engine is socket-based and HTTP/1.1 only.

## Accept Loop

- Create a `ServerSocket`.
- Bind to configured host and port.
- Accept sockets until stopped.
- Treat bind errors as startup failures.
- Treat shutdown during accept as normal.

## Connection Dispatch

Each accepted `Socket` is dispatched to the configured `ExecutorService`. If task submission is rejected, close the socket and produce a clear internal error path.

## Connection Lifecycle

For each connection:

1. Apply read, write, and idle timeouts.
2. Reuse one parser/input buffer for the connection.
3. Parse one HTTP/1.1 request.
4. Route it.
5. Write one response.
6. Continue the keep-alive loop when allowed, preserving parser leftovers.
7. Close on error, timeout, `Connection: close`, or shutdown.

The initial engine stays blocking and socket-based. A Java 17 NIO engine is future optional scope and must not replace the compatibility path without an ADR.

## Shutdown

Stopping the server should:

- stop accepting new connections;
- close the server socket;
- allow in-flight requests to finish where practical;
- close idle keep-alive sockets;
- shut down owned executors only.

## Errors

Handle bind errors, socket timeouts, unexpected disconnects, broken pipes, executor rejection, and shutdown races without noisy stack traces in normal paths.
