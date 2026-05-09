# GitHub Copilot Instructions

## Library

KissServer is a tiny, zero-dependency Java 17+ HTTP/1.1 socket server library.

## Public API Package

`io.github.arthurhoch.kiss.server`

## Main API Direction

```java
KissServer server = KissServer.create();
server.get("/health", ctx -> ctx.text("OK"));
server.get("/users/{id}", ctx -> ctx.text(ctx.pathParam("id")));
server.post("/echo", ctx -> ctx.text(ctx.bodyAsString()));
server.start(8080).await();
```

## Package Structure

- `io.github.arthurhoch.kiss.server` - facade and configuration
- `io.github.arthurhoch.kiss.server.http` - request, response, status, headers
- `io.github.arthurhoch.kiss.server.routing` - route matching and handlers
- `io.github.arthurhoch.kiss.server.protocol.http11` - parser and writer
- `io.github.arthurhoch.kiss.server.runtime` - accept loop, executor, shutdown
- `io.github.arthurhoch.kiss.server.buffer` - bounded buffer helpers
- `io.github.arthurhoch.kiss.server.errors` - exception hierarchy

## Constraints

- Java 17 only.
- Zero production dependencies.
- No frameworks.
- No Servlet API.
- No HTTP/2 in initial core.
- No TLS implementation in initial core.
- No reflection in the hot path.
- No parser regex, `String.split`, `Scanner`, or `BufferedReader.readLine`.
- JDK 21 virtual threads only through user-provided `ExecutorService`.
- Keep public API small and obvious.

## Testing

JUnit Jupiter. Tests must be deterministic and must not require internet access. Run `mvn -B verify`.

## Versioned AI Skills

Before creating a release tag, read `.github/skills-release-policy.md` and update the versioned Markdown skill artifacts under `docs/skills/`. Add a new `docs/skills/vX.Y.Z.md` file, update `docs/skills/index.md`, keep older skill files, and verify the complete public API/member index for the release.
