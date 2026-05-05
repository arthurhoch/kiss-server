# Getting Started

## Install

```xml
<dependency>
  <groupId>io.github.arthurhoch</groupId>
  <artifactId>kiss-server</artifactId>
  <version>0.1.0</version>
</dependency>
```

KissServer has zero production dependencies.

## First Route

```java
import io.github.arthurhoch.kiss.server.KissServer;

KissServer server = KissServer.create();
server.get("/health", ctx -> ctx.text("OK"));
server.start(8080).await();
```

The initial HTTP/1.1 socket engine is implemented for simple routes, request bodies with `Content-Length`, keep-alive, and safe default errors.

## Fast Health Route

Use the fast path for exact fixed `GET` endpoints:

```java
import io.github.arthurhoch.kiss.server.routing.FastResponses;

server.fastGet("/health", FastResponses.text("OK"));
```

## Dynamic Route

```java
server.get("/users/{id}", ctx -> ctx.text("User " + ctx.pathParam("id")));
```

## Query String

```java
server.get("/search", ctx -> ctx.text(ctx.request().queryString()));
```

## Echo

```java
server.post("/echo", ctx -> ctx.text(ctx.bodyAsString()));
```

## Next

- [Configuration](configuration.md)
- [Routing](routing.md)
- [Using kiss-server with AI coding agents](ai-usage.md)
- [Tutorial for AI agents and humans](tutorial-for-ai.md)
- [HTTP/1.1](http11.md)
- [Roadmap](roadmap.md)
