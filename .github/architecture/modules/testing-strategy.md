# Testing Strategy

## Required Tests

Future implementation must cover:

- parser valid requests;
- parser malformed requests;
- request line limit;
- header limit;
- header count limit;
- body limit;
- keep-alive;
- `Connection: close`;
- routing exact routes;
- routing dynamic routes;
- query params;
- path params;
- response writer;
- error mapping;
- fast path;
- real socket integration.

## Test Rules

- JUnit Jupiter.
- No internet access.
- No external services.
- Deterministic timing.
- Clear failure messages.

## Current Tests

The current suite covers configuration, executor behavior, parser valid and invalid requests, response writer output, routing, buffer pool, connection limiter, real socket behavior, keep-alive, request limits, safe error mapping, concurrency smoke tests, workflow sanity, and documentation sanity.
