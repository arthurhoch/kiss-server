# Using kiss-server with AI coding agents

This guide teaches AI coding agents how to generate small, correct, and performant applications with kiss-server.

## Purpose Of This Guide

AI agents tend to overbuild HTTP services: adding frameworks, JSON libraries, global abstractions, unnecessary maps, and hidden blocking work. KissServer is intentionally small. Generate code that matches that shape.

Use this guide when creating an app with kiss-server or when changing kiss-server itself.

## When To Use kiss-server

Use kiss-server for:

- simple Java 17+ APIs;
- local developer tools;
- small internal services;
- MVPs;
- embedded HTTP endpoints;
- AI-generated applications where readability and low dependency count matter;
- benchmarkable HTTP/1.1 services behind a reverse proxy.

## When Not To Use kiss-server

Do not choose kiss-server when the app requires:

- built-in HTTP/2;
- built-in TLS;
- WebSocket;
- Servlet API compatibility;
- Spring, Quarkus, Jakarta EE, annotation scanning, or dependency injection;
- built-in JSON binding;
- advanced static file serving;
- server features that are not implemented yet.

For TLS, HTTP/2, HTTP/3, compression policy, and edge caching, put Cloudflare, Nginx, Caddy, or another reverse proxy in front.

## Core Mental Model

```java
KissServer server = KissServer.create();

server.fastGet("/health", FastResponses.text("OK"));
server.get("/users/{id}", ctx -> ctx.text(ctx.pathParam("id")));
server.post("/echo", ctx -> ctx.text(ctx.bodyAsString()));

server.start(8080).await();
```

There are two route paths:

- fast path for exact, fixed `GET` responses;
- normal path for business routes.

## The Two Route Paths

### Fast Path

Fast path routes use `fastGet(path, responseBytes)`:

```java
server.fastGet("/health", FastResponses.text("OK"));
server.fastGet("/version", FastResponses.json("{\"version\":\"1.0.0\"}"));
```

Use fast path only when:

- method is `GET`;
- route is exact;
- response is fixed;
- no request body, path param, query param, database, file, or network call is needed.

Good candidates: `/health`, `/ready`, `/version`, `/ping`, `/robots.txt`.

### Normal Path

Normal routes use `get`, `post`, `put`, `delete`, `patch`, `head`, `options`, or `route`:

```java
server.get("/users/{id}", ctx -> {
    String id = ctx.pathParam("id");
    return ctx.text("User " + id);
});
```

Use normal routes for:

- dynamic path params;
- query-dependent responses;
- request body handling;
- business logic;
- validation;
- database, file, or network calls;
- custom response headers or status codes.

## Performance-First Route Selection

| Endpoint shape | Use | Reason |
|----------------|-----|--------|
| Fixed static `GET` | `fastGet` + `FastResponses` | avoids normal route object allocation |
| Dynamic `GET` | normal `get` | needs `Context` and path/query input |
| `POST` body | normal `post` | needs request body handling |
| Static JSON | `fastGet` + `FastResponses.json` | prebuilds response bytes |
| Dynamic JSON | normal route + `Response.body` | response depends on request or domain data |
| Blocking work | normal route + suitable executor | selector should not be blocked |

## Best Performance Patterns

- fixed static endpoint -> `fastGet` + `FastResponses`;
- dynamic endpoint -> normal route, minimal path/query parsing;
- POST endpoint -> avoid unnecessary body string conversion when bytes are enough;
- JSON response -> prebuilt string for static JSON, domain serializer for dynamic JSON;
- health/readiness -> fast path;
- metrics endpoint -> avoid expensive collection on every request;
- route handlers -> small, predictable, no global locks.

## Good Examples

Fast health endpoint:

```java
server.fastGet("/health", FastResponses.text("OK"));
```

Normal dynamic endpoint:

```java
server.get("/users/{id}", ctx -> {
    String id = ctx.pathParam("id");
    return ctx.text("User " + id);
});
```

Normal POST endpoint:

```java
server.post("/echo", ctx -> ctx.text(ctx.bodyAsString()));
```

JSON response without adding a JSON dependency:

```java
server.get("/status", ctx -> {
    String json = "{\"status\":\"ok\"}";
    return Response.body(HttpStatus.OK, ContentType.JSON, json, StandardCharsets.UTF_8);
});
```

Header access:

```java
server.get("/agent", ctx -> {
    String agent = ctx.request().header("User-Agent");
    return ctx.text(agent == null ? "" : agent);
});
```

Domain-specific body limit inside a route:

```java
server.post("/commands", ctx -> {
    byte[] body = ctx.request().body();
    if (body.length > 64 * 1024) {
        return ctx.text(HttpStatus.PAYLOAD_TOO_LARGE, "Payload too large");
    }
    return ctx.text("Accepted");
});
```

## Bad Examples

Do not use fast path for dynamic logic:

```java
// Bad: fast path is not for request-dependent business logic.
server.fastGet("/users/123", FastResponses.text(loadUserFromDatabase("123")));
```

Do not hide blocking work on direct mode:

```java
// Bad: DIRECT handlers run on the selector thread.
ServerConfig config = ServerConfig.builder()
        .handlerExecutionMode(HandlerExecutionMode.DIRECT)
        .build();
```

Do not add a framework just to build a small kiss-server app:

```xml
<!-- Bad for a simple kiss-server app unless the application explicitly needs it. -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

## Bad Patterns

- building huge strings per request;
- using synchronized global state in a hot route;
- doing blocking DB/file calls on event-loop/direct mode;
- parsing JSON manually in every route when not needed;
- allocating large temporary maps/lists per request;
- logging synchronously on every request;
- using fast path for dynamic business logic;
- adding Spring, Netty, Vert.x, or Undertow to an app just to use kiss-server.

## Java 17 Baseline

Generated code should default to Java 17:

- no JDK 21 APIs in required examples;
- no virtual-thread code unless clearly marked optional;
- no dependencies unless the app explicitly needs them;
- use `ExecutorService` from `java.util.concurrent`.

## JDK 21 Virtual Threads Optional Usage

JDK 21 applications may pass a virtual-thread executor:

```java
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

ServerConfig config = ServerConfig.builder()
        .port(8080)
        .executor(executor)
        .build();
```

This belongs in application code only. Do not add direct JDK 21 API calls to kiss-server main source.

## Android Compatibility Notes

KissServer is Android-compatible by design, not validated as universally Android-supported yet.

AI agents should:

- keep examples on Java 17 APIs;
- avoid JDK-internal APIs and JDK 21 requirements;
- avoid production dependencies;
- remember Android needs `android.permission.INTERNET`;
- mention lifecycle constraints such as foreground services, background restrictions, battery optimization, network changes, port binding, and LAN accessibility.

Do not claim Android device support until instrumented Android validation exists.

## Error Handling Patterns

Custom error handler API is not implemented yet. Current behavior maps safe server errors to HTTP responses such as `400`, `404`, `405`, `413`, `431`, and `500`.

For app-level validation, return explicit responses:

```java
server.get("/users/{id}", ctx -> {
    String id = ctx.pathParam("id");
    if (id == null || id.isBlank()) {
        return ctx.text(HttpStatus.BAD_REQUEST, "Missing id");
    }
    return ctx.text("User " + id);
});
```

Handler exceptions map to a safe `500 Internal Server Error` response and should not expose stack traces to clients.

## JSON Handling Without Dependency

KissServer does not require a JSON library. For static JSON:

```java
server.fastGet("/version", FastResponses.json("{\"version\":\"1.0.0\"}"));
```

For dynamic JSON, build or serialize the domain value in the application:

```java
server.get("/users/{id}", ctx -> {
    String id = escapeJson(ctx.pathParam("id"));
    String json = "{\"id\":\"" + id + "\"}";
    return Response.body(HttpStatus.OK, ContentType.JSON, json, StandardCharsets.UTF_8);
});
```

Only add a JSON dependency when the application explicitly needs one.

## Recommended Project Structure For Small Apps

```text
src/main/java/com/example/app/Main.java
src/main/java/com/example/app/Routes.java
src/main/java/com/example/app/UserService.java
src/test/java/com/example/app/RoutesTest.java
pom.xml
```

Keep route handlers small. Move domain logic into small classes when it improves clarity.

## Recommended Production Deployment

Use a reverse proxy for TLS and modern edge features:

```text
client
  -> Cloudflare/Nginx/Caddy using TLS + HTTP/2 or HTTP/3
      -> kiss-server using HTTP/1.1 locally
```

Configure an explicit executor for production apps. Choose a fixed or cached pool for Java 17 based on workload, or a virtual-thread executor from JDK 21 application code for blocking-heavy apps after testing.

## AI Checklist Before Generating Code

- Is Java 17 the default?
- Is kiss-server the only required HTTP server dependency?
- Are `/health`, `/ready`, `/version`, or other fixed endpoints using `fastGet`?
- Are dynamic and POST routes using normal handlers?
- Is `ExecutorService` configured explicitly for production examples?
- Are blocking calls kept off `DIRECT` mode and out of fast path?
- Are request body limits considered at server and app level?
- Is JSON handled as strings or by an explicitly requested app dependency?
- Are curl commands and tests included?

## AI Checklist Before Changing kiss-server Itself

- Read `AGENTS.md`, `.github/ALL_MARKDOWN.md`, and architecture docs first.
- Preserve Java 17 compatibility.
- Preserve zero production dependencies.
- Do not add framework, Servlet, HTTP/2, TLS, or WebSocket scope without an ADR.
- Keep parser code byte-oriented with bounded buffers.
- Do not add regex, `String.split`, `Scanner`, or `BufferedReader.readLine` to parser code.
- Keep reflection out of the hot path.
- Add or update tests for behavior changes.
- Update docs and benchmark notes before making performance claims.
- Run `mvn -B verify`.

## Common Mistakes To Avoid

- Using `fastGet` for request-dependent work.
- Calling `bodyAsString()` when raw bytes are enough.
- Adding Spring, Netty, Vert.x, Undertow, or a JSON library by default.
- Blocking selector/direct-mode handlers.
- Creating large temporary collections in hot routes.
- Logging every request synchronously in a benchmark.
- Claiming benchmark results are universal production guarantees.
- Mixing fast-static and normal-route results in one performance claim.
- Showing JDK 21 virtual-thread code as required library usage.
- Claiming Android or Native Image support before validation exists.

## Prompt Snippet For Generating A kiss-server App

```text
Create a Java 17 Maven app using kiss-server. Keep the app zero-framework and use no dependencies except kiss-server unless I explicitly request one. Use fastGet with FastResponses for fixed /health and /version endpoints. Use normal get/post routes for dynamic and business endpoints. Configure an explicit ExecutorService. Include curl commands, focused tests, and a short benchmark command using wrk. Do not use JDK 21 APIs unless the snippet is clearly marked optional.
```

## Prompt Snippet For Optimizing A kiss-server App

```text
Inspect this kiss-server app for performance. Identify fixed static GET endpoints that can safely use fastGet. Keep dynamic and POST routes on the normal path. Look for unnecessary allocation, body string conversion, synchronized hot state, blocking direct-mode handlers, and synchronous per-request logging. Benchmark before and after with wrk, include p99 latency, store raw results, and do not claim universal production performance.
```

## Prompt Snippet For Adding A New Route

```text
Add a route to this kiss-server app. Inspect the current routing style first. Use fastGet only if the route is an exact fixed GET response. Use a normal route for dynamic params, query-dependent logic, request bodies, or business logic. Keep Java 17 compatibility, add focused tests, update docs or curl examples, and avoid adding frameworks or JSON dependencies unless required.
```
