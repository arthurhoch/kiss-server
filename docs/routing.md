# Routing

## Exact Routes

```java
server.get("/health", ctx -> ctx.text("OK"));
```

Exact routes should be checked before dynamic routes.

## Dynamic Routes

```java
server.get("/users/{id}", ctx -> ctx.text(ctx.pathParam("id")));
```

Route params are extracted by segment. Regex route matching is not part of the v1 design.

## Matching Order

1. Fast exact routes.
2. Normal exact routes.
3. Dynamic routes.
4. 404.
5. 405 when another method matches the path.
