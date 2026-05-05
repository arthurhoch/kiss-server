# Fast Path

The fast path is an optional route path for exact, fixed `GET` responses.

## What It Is

Fast path routes are registered with `fastGet`:

```java
server.fastGet("/health", FastResponses.text("OK"));
server.fastGet("/version", FastResponses.json("{\"version\":\"1.0.0\"}"));
```

The response is prebuilt as bytes. The NIO engine can serve it without creating normal `Request`, `Context`, or `Response` objects.

## What It Is Not

The fast path is not:

- a general handler API;
- a replacement for normal routes;
- a place for database, file, or network calls;
- a place for request-dependent business logic;
- dynamic route support;
- POST body handling;
- a reason to complicate the normal public API.

## Why It Exists

Many services have fixed endpoints such as `/health`, `/ready`, `/version`, `/ping`, and `/robots.txt`. These endpoints are often hit frequently by load balancers, orchestrators, and monitoring tools.

When the response is fixed, building request and response helper objects on every call is unnecessary. The fast path keeps those endpoints cheap while leaving normal routes readable.

## When To Use It

Use fast path when all are true:

- method is `GET`;
- path is exact;
- response is fixed;
- no request body is needed;
- no path param is needed;
- no query string is needed;
- no header inspection is needed;
- no database, file, network, or blocking work is needed.

Good candidates:

- `/health`;
- `/ready`;
- `/version`;
- `/ping`;
- `/robots.txt`.

## When Not To Use It

Use a normal route when:

- response depends on a user, path param, query string, header, body, or time;
- route performs validation;
- route calls a database, file system, or remote service;
- route returns dynamic JSON;
- route needs custom response composition;
- route handles `POST`, `PUT`, `PATCH`, or `DELETE`.

## Example Usage

```java
import io.github.arthurhoch.kiss.server.KissServer;
import io.github.arthurhoch.kiss.server.routing.FastResponses;

KissServer server = KissServer.create();

server.fastGet("/health", FastResponses.text("OK"));
server.fastGet("/version", FastResponses.json("{\"version\":\"1.0.0\"}"));

server.get("/users/{id}", ctx -> ctx.text(ctx.pathParam("id")));
```

## Why Fixed Static Responses Are Faster

Fixed static responses can be prepared once:

- status line;
- `Content-Type`;
- `Content-Length`;
- body bytes.

At request time the engine only needs to add connection handling and write bytes. Normal routes remain more flexible but allocate more objects.

## Benchmark Relevance

Fast path results are useful for fixed endpoints and for tracking the lower overhead bound of the HTTP engine. They do not represent dynamic business route performance.

The benchmark app labels fast path runs with `fast-static`. Reports must preserve that label.

## Fairness Caveat

The latest benchmark snapshot includes winning kiss-server simple endpoint results that use fast-static mode for `/health`, `/hello`, and `/json`. Dynamic and POST workloads are reported separately and currently have different bottlenecks.
