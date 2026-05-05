# Fast Path

The fast path exists for exact static `GET` responses.

## Contract

- Exact route only.
- `GET` only through the public `fastGet` helper.
- Prebuilt response bytes from `FastResponses` or equivalent valid HTTP/1.1 bytes.
- No dynamic path params.
- No query/header/body-dependent logic.
- No database, file, network, sleep, or blocking work.
- No normal `Request`, `Context`, or `Response` object creation unless the implementation changes by documented ADR.

## Intended Uses

- `/health`;
- `/ready`;
- `/version`;
- `/ping`;
- `/robots.txt`.

## Non-Uses

- dynamic user routes;
- POST routes;
- business commands;
- per-request metrics collection;
- routes that need auth, headers, query parsing, or body parsing.

## Public API Rule

The fast path must not distort the normal route API. It should remain an optional optimization for simple exact endpoints.

## Benchmark Rule

Any benchmark using fast path must be labeled `fast-static` or another explicit fast-path label. Do not mix fast-static results with normal handler results.
