# AI Behavior Rules

Strict rules for AI coding agents working on KissServer.

## Mandatory Reading

Read, in order:

1. `AGENTS.md`
2. `.github/ALL_MARKDOWN.md`
3. `.github/architecture/index.md`
4. Relevant module docs

## Do Not

- Do not add production dependencies.
- Do not create a framework.
- Do not add Spring, Quarkus, Netty, Jetty, Undertow, or Servlet API.
- Do not implement HTTP/2 unless an ADR changes the scope.
- Do not implement TLS or WebSocket in the initial core.
- Do not use reflection in the hot path.
- Do not use regex, `String.split`, `Scanner`, or `BufferedReader.readLine` in the HTTP parser.
- Do not silently change public API behavior.
- Do not skip tests.
- Do not skip docs.
- Do not claim unimplemented behavior as working.
- Do not commit secrets or generated build output.

## Always

- Keep Java 17 compatibility.
- Keep code simple and explicit.
- Enforce bounded limits.
- Update docs and README examples when public API changes.
- Update `CHANGELOG.md` for user-visible changes.
- Run `mvn -B verify` before considering work complete.
