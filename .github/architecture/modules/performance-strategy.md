# Performance Strategy

KissServer performance work must preserve the KISS shape: small code, bounded buffers, zero production dependencies, and a simple public API.

## Current Model

- Java 17 NIO selector engine.
- Normal route handlers run on `ExecutorService` by default.
- Fast static exact `GET` routes may be served without normal route object allocation.
- Exact routes are checked before dynamic routes.
- Response writing computes `Content-Length`.
- Undertow and Vert.x are benchmark references only.

## Benchmark Claim Rules

- Store raw benchmark output.
- Record JDK, OS, heap, command, warmup, duration, and date.
- Report p99 latency with requests/sec.
- Label fast-static results separately.
- Separate dynamic and POST workloads from static endpoint results.
- Do not claim kiss-server is always faster than Undertow or Vert.x.
- Treat localhost numbers as directional, not universal production guarantees.

## Latest Snapshot Used In Docs

Raw result directory:

```text
benchmarks/results/20260504T211305Z-nio-rerun
```

Environment:

- Java: Temurin 21.0.11;
- wrk: 4.2.0 [kqueue];
- heap: `-Xms512m -Xmx512m`;
- localhost;
- 10s warmup;
- 30s measured;
- `--latency`.

## Optimization Targets

- dynamic route path param allocation;
- normal route `Request`/`Context`/`Response` allocation;
- POST body handling;
- p99 latency;
- selector/worker handoff cost.

Every optimization needs focused tests. Shared runtime changes also need integration tests.
