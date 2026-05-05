# API

Public API is intentionally small.

## `KissServer`

```java
KissServer server = KissServer.create();
KissServer configured = KissServer.create(config);
```

Route helpers:

```java
server.get(path, handler);
server.post(path, handler);
server.put(path, handler);
server.delete(path, handler);
server.patch(path, handler);
server.head(path, handler);
server.options(path, handler);
server.route(HttpMethod.GET, path, handler);
server.direct(HttpMethod.GET, path, directHandler);
server.fastGet(path, FastResponses.text("OK"));
```

`fastGet` is for exact static `GET` endpoints such as `/health`. Normal routes remain the default API for application behavior.

Start helpers:

```java
ServerHandle handle = server.start();
ServerHandle handle = server.start(8080);
```

`start` binds a blocking HTTP/1.1 socket server. Use port `0` in tests to let the OS choose a free port.

## `ServerConfig`

Immutable configuration with `ServerConfig.builder()`. See [Configuration](configuration.md).

## `ServerHandle`

```java
int port();
boolean running();
void await() throws InterruptedException;
void stop();
void close();
```

## HTTP Types

- `HttpMethod`
- `HttpStatus`
- `HttpHeaders`
- `ContentType`
- `Request`
- `Response`
- `Context`

Useful current request helpers:

```java
ctx.request().header("Content-Type");
ctx.request().queryString();
ctx.request().body();
ctx.bodyAsString();
ctx.pathParam("id");
```

JSON responses are string-based in the normal path:

```java
Response.body(HttpStatus.OK, ContentType.JSON, json, StandardCharsets.UTF_8);
```

Static fast responses are prebuilt bytes:

```java
FastResponses.text("OK");
FastResponses.json("{\"status\":\"ok\"}");
```

## Routing Types

- `Handler`
- `DirectHandler`
- `Router`
- `Route`
- `RouteMatch`
- `RouteTrie`
- `FastResponses`
