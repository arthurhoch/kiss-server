---
layout: default
---

# Prompt: Optimize A kiss-server App

```text
Inspect this kiss-server application for performance.

Instructions:
- Profile or benchmark first before changing code.
- Identify exact fixed GET endpoints that can safely use fastGet + FastResponses.
- Keep dynamic and POST routes on the normal path.
- Avoid unnecessary allocation in hot handlers.
- Avoid repeated body string conversion when bytes are enough.
- Avoid synchronized global state in hot routes.
- Avoid synchronous per-request logging in benchmark paths.
- Do not block selector/direct-mode handlers with database, file, network, sleep, or slow work.
- Configure ExecutorService appropriately for the workload.
- Benchmark before and after with wrk.
- Include p99 latency, requests/sec, socket errors, command, JDK, heap, and raw results location.
- Do not fake results or claim universal production performance from localhost benchmarks.
```
