# Benchmarking Strategy

Benchmarks are for validation and regression detection. They must not make normal CI slow.

## Tools

- `wrk` for end-to-end HTTP throughput.
- `autocannon` for Node-based HTTP load testing.
- JMH for parser, router, and writer microbenchmarks only.

## Example Commands

```bash
wrk -t4 -c256 -d30s http://127.0.0.1:8080/health
autocannon -c 256 -d 30 http://127.0.0.1:8080/health
```

## Optional Comparisons

Current benchmark references are Undertow and Vert.x in isolated benchmark apps. They must not become main-artifact dependencies. Do not claim superiority without measured results.

KissServer benchmark labels:

- `kiss-server-nio-worker-fast-static`
- `kiss-server-nio-direct-fast-static`
- `kiss-server-nio-virtual-threads-jdk21-fast-static`

Fast path benchmarks must use a separate label and must not be mixed with normal handler results.

## Metrics

- requests/sec;
- p50;
- p95;
- p99;
- errors;
- allocation rate;
- GC;
- CPU;
- memory.

Record hardware, OS, JDK, command, warmup, and date.

## Claim Rules

- Include raw result files.
- Include p99 latency with throughput.
- Separate fast-static, dynamic, and POST scenarios.
- Treat localhost results as directional, not universal production guarantees.
- Do not claim kiss-server is always faster than Undertow or Vert.x.
