---
layout: default
---

# Prompt: Benchmark A kiss-server App

```text
Benchmark this kiss-server application fairly.

Requirements:
- Use wrk with --latency.
- Run a warmup before measured runs.
- Measure p99 latency and requests/sec.
- Store raw wrk output under a timestamped results directory.
- Record JDK distribution/version, heap flags, OS, machine, command, warmup duration, measured duration, and commit.
- Label fast-static endpoints separately from normal dynamic and POST routes.
- Check socket errors and non-2xx/3xx responses.
- Compare only equivalent routes, payloads, heap, JDK, and concurrency.
- Do not report only best numbers without raw results.
- Do not claim universal production guarantees from local results.
```
