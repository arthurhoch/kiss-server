# AI Usage

This module guides future AI coding agents that generate applications with kiss-server or modify kiss-server itself.

## App Generation Rules

- Default examples to Java 17.
- Use kiss-server as the HTTP server dependency.
- Do not add Spring, Quarkus, Netty, Jetty, Undertow, Vert.x, Servlet, DI, or annotation scanning unless the user explicitly asks for that separate application stack.
- Do not add a JSON dependency unless the application explicitly needs one.
- Configure an `ExecutorService` explicitly in production-style examples.
- Keep route handlers small and readable.
- Include curl commands and focused tests when generating an app.

## Route Selection Rules

- Fixed static exact `GET` endpoint -> `fastGet` + `FastResponses`.
- Dynamic endpoint -> normal route.
- POST endpoint -> normal route.
- Business logic -> normal route.
- Blocking work -> normal route with an appropriate executor.
- Direct mode -> only for controlled, non-blocking, fast handlers.

Good fast path candidates:

- `/health`;
- `/ready`;
- `/version`;
- `/ping`;
- `/robots.txt`.

## Performance Rules

- Reuse static response strings/bytes.
- Avoid unnecessary body string conversion.
- Avoid large temporary maps/lists in hot handlers.
- Avoid synchronized global state in hot routes.
- Avoid synchronous per-request logging in benchmarks.
- Do not hide database, file, or network calls in fast path or direct mode.
- Benchmark before and after optimization.
- Label fast-static results separately from normal-route results.

## Change Rules For kiss-server Core

- Preserve Java 17 source compatibility.
- Preserve zero production dependencies.
- Keep parser code byte-oriented and bounded.
- Do not add regex, `String.split`, `Scanner`, or `BufferedReader.readLine` to parser code.
- Keep reflection out of the hot path.
- Update tests and docs for behavior changes.
- Do not make performance claims without raw results and environment details.
- Run `mvn -B verify`.
