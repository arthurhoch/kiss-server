# Testing Report

Date: 2026-05-08

## What Was Tested

- `ServerConfig` defaults, builder values, validation, immutability via `withPort`, executor null/user handling, keep-alive, timeout, and buffer settings.
- HTTP models: request defensive copies, repeatable body reads, case-insensitive header lookup, context path params, text responses, custom statuses, content types, and header injection rejection.
- Router behavior: exact routes by method, same path with different methods, dynamic params, multiple params, exact route priority, fast path priority, trailing slash behavior, invalid route definitions, and 405 method discovery.
- HTTP/1.1 parser: valid GET/HEAD/POST/query requests, headers with optional whitespace, multiple headers, `Connection` headers, malformed request lines, unsupported versions, invalid methods, bad headers, invalid CRLF, invalid `Content-Length`, short bodies, request line limits, header limits, header count limits, and body limits.
- HTTP/1.1 response writer: common statuses, CRLF formatting, `Content-Length`, `Content-Type`, `Connection`, HEAD output, 204 output, exact body bytes, and duplicate `Content-Length` prevention with case-insensitive header names.
- Real socket behavior: start on port `0`, stop, GET `/health`, POST `/echo`, path params, query string preservation, 404, 405, safe 500 responses, malformed requests, oversized headers, oversized bodies, keep-alive, `Connection: close`, `maxKeepAliveRequests`, idle timeout, fast path, rejected executor handling, user executor ownership, repeated start/stop, and small concurrent load.
- Runtime helpers: buffer pool acquire/release/reuse, pool max size, input/output buffer bounds, and connection limiter capacity/accounting.
- Project sanity: parser source avoids forbidden line parsing APIs, workflows use Java 17/21 and `mvn -B clean verify`, Pages uses `docs/`, Maven Central secrets are placeholders, Dependabot monitors Maven and Actions, and Native Image docs do not claim official support.
- Coverage: JaCoCo XML and HTML reports are generated under `target/site/jacoco/`.

## Commands Run

```bash
mvn -B clean verify
mvn -B javadoc:javadoc
mvn -B -Pspotbugs -DskipTests verify
mvn -B -Psecurity -Ddependency-check.skip=true -DskipTests verify
```

Results:

- `mvn -B clean verify`: passing from a clean workspace, 60 tests, 0 failures, 0 errors.
- `mvn -B javadoc:javadoc`: passing.
- `mvn -B -Pspotbugs -DskipTests verify`: passing profile validation.
- `mvn -B -Psecurity -Ddependency-check.skip=true -DskipTests verify`: passing profile validation with Dependency-Check database scanning intentionally skipped.
- JaCoCo reports: `target/site/jacoco/jacoco.xml` and `target/site/jacoco/index.html` generated.

## Important Edge Cases Covered

- Header name ASCII validation.
- Response header CRLF injection rejection.
- Case-insensitive `Content-Length` handling.
- Empty dynamic path segments are not collapsed.
- Parser rejects ambiguous or malformed CRLF.
- Handler exceptions return a safe `500` body without stack traces.
- Rejected executor submission closes the socket without stopping the server.
- User-provided executors are not shut down by `ServerHandle.stop()`.
- Cleartext socket and loopback `HttpClient` test probes are isolated in `LoopbackHttpTestClient`; `.dcignore` excludes only that helper to avoid false positives while leaving production code and ordinary tests scanned.

## Known Limitations

- No custom error handler API yet.
- No query parameter decoding helper yet; handlers can read the raw query string.
- No chunked request body support.
- No static file serving.
- No TLS, HTTP/2, HTTP/3, or WebSocket support.
- Fast path responses are prebuilt bytes; callers are responsible for any extra headers beyond the helper output.
- Native Image friendliness is designed and documented but not validated with a native build.
- Manual benchmarks have not been recorded yet.

## Future Tests Recommended

- More malformed request fuzz cases.
- Percent-decoding behavior if query helpers are added.
- More connection limiter integration cases with held-open sockets.
- Native Image example app validation.
- JMH parser/router/writer microbenchmarks.
- Manual `wrk` and `autocannon` benchmark runs recorded with hardware and JDK details.
