# CAVEMAN.md - Compact Summary

Read this first when context is tight.

## What This Is

KissServer: tiny Java 17+ HTTP/1.1 socket server. Zero production dependencies. Simple APIs, local tools, MVPs, internal services, embedded use.

Sibling projects: kiss-requests is the HTTP client, kiss-json is the JSON library. KissServer does not depend on them.

## Main Mental Model

```java
KissServer server = KissServer.create();
server.get("/health", ctx -> ctx.text("OK"));
server.get("/users/{id}", ctx -> ctx.text(ctx.pathParam("id")));
server.post("/echo", ctx -> ctx.text(ctx.bodyAsString()));
server.start(8080).await();
```

## KISS Rules

- Simple code.
- No clever magic.
- No framework.
- No production dependency.
- No HTTP/2.
- No servlet.
- No reflection hot path.
- No parser hacks.
- Test everything.
- Document changes.
- Build must pass.

## Parser Rules

- Byte-oriented HTTP/1.1 parser.
- No regex.
- No `String.split`.
- No `Scanner`.
- No `BufferedReader.readLine`.
- Enforce request line, header, body, timeout, connection, and keep-alive limits.

## Runtime Rules

- Socket-based engine.
- User may provide `ExecutorService`.
- If user provided executor, do not shut it down automatically.
- If KissServer created executor, shut it down on stop.
- Java 17 baseline.
- JDK 21 users may pass virtual-thread executor from application code.

## Before Coding

Read:

1. `AGENTS.md`
2. `.github/ALL_MARKDOWN.md`
3. `.github/architecture/index.md`
4. Relevant architecture module
5. Relevant docs page

Then make the smallest correct change, add tests, update docs, and run:

```bash
mvn -B verify
```
