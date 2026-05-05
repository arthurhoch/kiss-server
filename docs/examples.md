# Examples

These examples show the current public API.

## Health

```java
KissServer server = KissServer.create();
server.get("/health", ctx -> ctx.text("OK"));
server.start(8080).await();
```

For a fixed health response, prefer the fast path:

```java
server.fastGet("/health", FastResponses.text("OK"));
```

## Path Parameter

```java
server.get("/users/{id}", ctx -> {
    String id = ctx.pathParam("id");
    return ctx.text("User " + id);
});
```

## POST Echo

```java
server.post("/echo", ctx -> ctx.text(ctx.bodyAsString()));
```

## Query String

```java
server.get("/search", ctx -> ctx.text(ctx.request().queryString()));
```

## Header Access

```java
server.get("/agent", ctx -> {
    String agent = ctx.request().header("User-Agent");
    return ctx.text(agent == null ? "" : agent);
});
```

## Custom Status

```java
server.get("/missing", ctx -> ctx.text(HttpStatus.NOT_FOUND, "Missing"));
```

## Configured Server

```java
ExecutorService executor = Executors.newCachedThreadPool();

ServerConfig config = ServerConfig.builder()
        .port(8080)
        .executor(executor)
        .maxConnections(10_000)
        .build();

KissServer.create(config)
        .get("/health", ctx -> ctx.text("OK"))
        .start();
```

## Fast Response

```java
server.fastGet("/health", FastResponses.text("OK"));
```

## JSON Response

```java
String json = "{\"message\":\"hello\"}";
server.get("/json", ctx -> Response.body(HttpStatus.OK, ContentType.JSON, json, StandardCharsets.UTF_8));
```

## Optional JDK 21 Virtual Threads

```java
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

ServerConfig config = ServerConfig.builder()
        .port(8080)
        .executor(executor)
        .build();
```

Keep this call in application code. KissServer main source compiles on Java 17.
