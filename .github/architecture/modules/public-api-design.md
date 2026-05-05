# Public API Design

The target public API is small and memorable:

```java
KissServer server = KissServer.create();
server.get("/health", ctx -> ctx.text("OK"));
server.get("/users/{id}", ctx -> ctx.text(ctx.pathParam("id")));
server.post("/echo", ctx -> ctx.text(ctx.bodyAsString()));
server.start(8080).await();
```

## Configuration

Configuration uses an immutable `ServerConfig` with a builder. Avoid adding options until they are needed by real behavior.

## Public Packages

Public types may live under:

- `io.github.arthurhoch.kiss.server`;
- `io.github.arthurhoch.kiss.server.http`;
- `io.github.arthurhoch.kiss.server.routing`;
- `io.github.arthurhoch.kiss.server.errors`.

Protocol, runtime, and buffer packages are implementation detail unless documented otherwise.

## API Change Rule

Any public API change requires docs, README examples, tests, and changelog updates.
