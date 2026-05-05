# Benchmarking

Manual benchmark apps live under `benchmarks/`. They compare kiss-server NIO execution profiles against Undertow and Vert.x reference apps.

Undertow and Vert.x are benchmark references only. They are isolated under `benchmarks/apps/` and are not dependencies of the main kiss-server artifact.

Normal `mvn -B verify` does not build or run benchmarks.

## Benchmark Labels

Current labels:

- `kiss-server-nio-worker-fast-static`
- `kiss-server-nio-direct-fast-static`
- `kiss-server-nio-virtual-threads-jdk21-fast-static`
- `undertow`
- `vertx`

`fast-static` means exact static `GET /health`, `GET /hello`, and `GET /json` use `fastGet` with prebuilt responses. Do not present those numbers as normal handler results.

To disable fast static routes:

```bash
KISS_FAST_STATIC=false KISS_MODE=worker PORT=8080 \
  java -Xms512m -Xmx512m -jar benchmarks/apps/kiss-server-app/target/kiss-server-app.jar
```

Use labels without `fast-static` when fast static routes are disabled.

## Build Benchmark Apps

Install the local kiss-server artifact:

```bash
mvn -B install -DskipTests
```

Build the isolated benchmark apps:

```bash
mvn -B -f benchmarks/apps/kiss-server-app/pom.xml package
mvn -B -f benchmarks/apps/undertow-app/pom.xml package
mvn -B -f benchmarks/apps/vertx-app/pom.xml package
```

The `kiss-server-app` benchmark module may compile with JDK 21 so it can call `Executors.newVirtualThreadPerTaskExecutor()` in benchmark application code. This does not change the Java 17 baseline of the main artifact.

## Run kiss-server Benchmark Modes

Run one server at a time:

```bash
KISS_MODE=worker PORT=8080 java -Xms512m -Xmx512m \
  -jar benchmarks/apps/kiss-server-app/target/kiss-server-app.jar
```

```bash
KISS_MODE=direct PORT=8080 java -Xms512m -Xmx512m \
  -jar benchmarks/apps/kiss-server-app/target/kiss-server-app.jar
```

```bash
KISS_MODE=virtual-threads PORT=8080 java -Xms512m -Xmx512m \
  -jar benchmarks/apps/kiss-server-app/target/kiss-server-app.jar
```

Label the virtual-thread mode exactly as `kiss-server on JDK 21 with virtual-thread executor`.

## Run Undertow And Vert.x Reference Apps

```bash
PORT=8080 java -Xms512m -Xmx512m \
  -jar benchmarks/apps/undertow-app/target/undertow-app.jar
```

```bash
PORT=8080 java -Xms512m -Xmx512m \
  -jar benchmarks/apps/vertx-app/target/vertx-app.jar
```

Use the same Java version, heap settings, machine, endpoint payloads, warmup, duration, concurrency, and benchmark tool for every server.

## Run The Scripted wrk Suite

The script performs a 10 second warmup before each scenario and writes each 30 second measured result to its own file. All scripted runs use `--latency`.

```bash
BASE_URL=http://127.0.0.1:8080 OUT_DIR=benchmarks/results/kiss-server-nio-worker-fast-static \
  ./benchmarks/scripts/run-all.sh kiss-server-nio-worker-fast-static
```

```bash
BASE_URL=http://127.0.0.1:8080 OUT_DIR=benchmarks/results/kiss-server-nio-direct-fast-static \
  ./benchmarks/scripts/run-all.sh kiss-server-nio-direct-fast-static
```

```bash
BASE_URL=http://127.0.0.1:8080 OUT_DIR=benchmarks/results/kiss-server-nio-virtual-threads-jdk21-fast-static \
  ./benchmarks/scripts/run-all.sh kiss-server-nio-virtual-threads-jdk21-fast-static
```

```bash
BASE_URL=http://127.0.0.1:8080 OUT_DIR=benchmarks/results/undertow \
  ./benchmarks/scripts/run-all.sh undertow
```

```bash
BASE_URL=http://127.0.0.1:8080 OUT_DIR=benchmarks/results/vertx \
  ./benchmarks/scripts/run-all.sh vertx
```

## Exact wrk Commands

Warmup shape:

```bash
wrk --latency -t8 -c500 -d10s http://127.0.0.1:8080/health
```

Measured scenarios:

```bash
wrk --latency -t4 -c100 -d30s http://127.0.0.1:8080/health
wrk --latency -t8 -c500 -d30s http://127.0.0.1:8080/health
wrk --latency -t8 -c500 -d30s http://127.0.0.1:8080/hello
wrk --latency -t8 -c500 -d30s http://127.0.0.1:8080/json
wrk --latency -t8 -c500 -d30s 'http://127.0.0.1:8080/users/123?active=true'
wrk --latency -t8 -c500 -s benchmarks/scripts/post-echo.lua -d30s http://127.0.0.1:8080/echo
wrk --latency -t8 -c500 -s benchmarks/scripts/post-consume.lua -d30s http://127.0.0.1:8080/consume
```

POST payload used by the Lua scripts:

```json
{"name":"Arthur","message":"hello","value":123}
```

## Reading p99

p99 latency is the latency below which 99 percent of observed requests completed. It matters because a throughput win can still be a bad trade if tail latency gets worse for a latency-sensitive service.

Compare:

- requests/sec;
- average latency;
- p50, p90, and p99 latency;
- socket errors;
- non-2xx/3xx responses;
- CPU usage;
- memory usage;
- GC behavior.

## Compare Fairly

Fair comparison requires:

- same machine;
- same OS and kernel state as much as practical;
- same JDK distribution and version;
- same JVM flags and heap;
- same endpoint semantics and payloads;
- same warmup and measured duration;
- same concurrency and thread count;
- same benchmark tool and options;
- raw results stored for audit.

Localhost benchmarks are useful for repeatable development checks, but they are not final production truth. Real deployments include network devices, TLS, proxies, containers, CPU quotas, kernel settings, payload variance, and client geography.

## Why Fast-Static Results Must Be Labeled

The kiss benchmark app uses fast path for exact static `GET /health`, `GET /hello`, and `GET /json` by default. This avoids normal `Request`, `Context`, and `Response` object creation.

That is valid for fixed static endpoints, but it is not the same workload as a normal route. Dynamic and POST scenarios must be reported separately.

## Raw Results

Latest raw results used by the docs:

```text
benchmarks/results/20260504T211305Z-nio-rerun
```

Keep raw `wrk` files, server logs, run logs, environment notes, date, and commit with every benchmark summary.

## Add A New Benchmark Scenario

1. Add the endpoint to all benchmark apps with equivalent behavior.
2. Add any required Lua script under `benchmarks/scripts/`.
3. Add a `run_scenario` entry to `benchmarks/scripts/run-all.sh`.
4. Document whether the route is fast path or normal path.
5. Run all compared servers with the same settings.
6. Store raw results under `benchmarks/results/<timestamp>-<label>/`.
7. Summarize requests/sec and p99 without deleting raw files.

## Avoid Fake Benchmarks

- Do not benchmark a route that returns different payloads across servers.
- Do not mix fast-static and normal-route results.
- Do not ignore socket errors or non-2xx/3xx responses.
- Do not change heap or JDK between servers.
- Do not report only best single runs without raw output.
- Do not include synchronous logging in one app but not the others.
- Do not claim universal production performance from localhost results.
- Do not call Undertow or Vert.x dependencies of kiss-server.
