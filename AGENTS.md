# AGENTS.md - Primary AI Agent Instructions

This is the primary instruction file for AI coding agents working on KissServer.

## Mandatory Reading Order

Before making changes, read these files in order:

1. `AGENTS.md` - this file.
2. `.github/ALL_MARKDOWN.md` - complete documentation index.
3. `.github/architecture/index.md` - architecture reading order.
4. Relevant module docs under `.github/architecture/modules/`.
5. Relevant user docs under `docs/`.

`CAVEMAN.md` is a compact summary and is useful for quick context, but this file and the architecture docs are authoritative.

## Project Purpose

KissServer is a tiny, zero-dependency Java 17+ HTTP/1.1 socket server library for simple APIs, local tools, MVPs, internal services, embedded use cases, AI-generated applications, and Android-compatible scenarios where possible.

It is a sibling of `kiss-requests` and `kiss-json`. Use those repositories as ecosystem, documentation, workflow, and style references only. Do not depend on them.

## Non-Negotiable KISS Rules

1. Zero production dependencies.
2. Java 17 compatibility.
3. Simple public API.
4. No frameworks.
5. No Servlet API.
6. No HTTP/2 unless an ADR changes the scope.
7. No TLS implementation in the initial core.
8. No WebSocket in the initial core.
9. No reflection in the hot path.
10. No regex, `String.split`, `Scanner`, or `BufferedReader.readLine` in the HTTP parser.
11. Bounded buffers and explicit limits.
12. Clear errors and predictable shutdown.

## Coding Rules

- Public API package: `io.github.arthurhoch.kiss.server`.
- Keep classes small with clear responsibilities.
- Prefer explicit names over clever abstractions.
- Do not add production dependencies without updating the dependency policy and creating an ADR.
- Do not introduce Spring, Quarkus, Netty, Jetty, Undertow, Servlet, DI, annotation scanning, or service-loader based core behavior.
- Preserve Java 17 source compatibility. Do not call JDK 21-only APIs from main source.
- JDK 21 virtual threads are supported by user-provided `ExecutorService`.
- Optional reflection helpers must stay outside the hot path and must be documented.
- Sanitize response headers and reject invalid input.
- Restore interrupt flags when handling interruption.
- Never commit `target/`, `.DS_Store`, IDE files, logs, or generated build output.

## Parser Rules

- Parse HTTP/1.1 bytes directly.
- Enforce request line, header, header count, body, keep-alive, timeout, and connection limits.
- Reject malformed request lines, invalid header names, unsupported versions, oversized headers, and oversized bodies.
- Do not trust `Content-Length` blindly.
- Avoid uncontrolled buffer growth.
- Do not use regex, `String.split`, `Scanner`, or `BufferedReader.readLine` in parser code.

## Using kiss-server Performantly

- Prefer `fastGet` with `FastResponses` for fixed exact responses such as `/health`, `/ready`, `/version`, `/ping`, and `/robots.txt`.
- Use normal routes for dynamic paths, query-dependent behavior, request bodies, validation, and business logic.
- Avoid unnecessary allocation in hot handlers, including large temporary maps, lists, and repeated body string conversion.
- Do not add dependencies casually. Keep the main artifact zero production dependency.
- Do not call JDK 21 APIs in main source. Virtual-thread executors belong in application or benchmark code only.
- Keep Android compatibility in mind by staying on Java 17-compatible standard APIs where reasonable.
- Update docs and tests when behavior or public examples change.
- Benchmark before making performance claims, keep raw results, and label fast-static results separately from normal dynamic and POST route results.

## Documentation Rules

- Update docs when behavior changes.
- Update README examples when public API changes.
- Update `.github/ALL_MARKDOWN.md` when markdown files are added, renamed, or removed.
- Keep GitHub Pages docs in sync with README.
- Explain trade-offs in architecture docs.
- Use `Intended contract` or `Planned` for behavior that is not implemented yet.
- Do not claim the HTTP engine or Native Image support is complete until validated.

## Testing Rules

- Add or update tests with every behavior change.
- Tests must be deterministic and must not require internet access.
- Use JUnit Jupiter.
- Run `mvn -B verify` before claiming completion.
- Future protocol work must include parser, routing, writer, error, keep-alive, and real socket integration tests.

## Maven Central Rules

- Coordinates: `io.github.arthurhoch:kiss-server`.
- Keep required metadata correct in `pom.xml`.
- Publish only through the release workflow and `release` Maven profile.
- Do not hardcode secrets.
- Required secrets: `MAVEN_CENTRAL_USERNAME`, `MAVEN_CENTRAL_PASSWORD`, `GPG_PRIVATE_KEY`, `GPG_PASSPHRASE`.

## Change Protocol

1. Read the mandatory docs.
2. Read the specific package and tests you are changing.
3. Make the smallest correct change.
4. Add or update tests.
5. Update docs and changelog for public behavior.
6. Run `mvn -B verify`.
7. Report what changed, what was verified, and what remains.
