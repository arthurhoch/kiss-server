# KissServer Benchmarks

This directory contains manual benchmark apps for comparing `kiss-server` only against Undertow and Vert.x.

Undertow and Vert.x are used because they are established, high-performance, embeddable Java HTTP servers with no servlet container requirement for these apps. The benchmark intentionally does not include Jetty, Spring, Quarkus, Netty directly, Javalin, NanoHTTPD, or JDK `HttpServer`; each additional server needs separate fairness work and route parity.

These benchmarks are not part of normal `mvn verify`.

## Apps

Each app exposes the same endpoints and response bodies:

- `GET /health`: `200`, `Content-Type: text/plain`, body `OK`
- `GET /hello`: `200`, `Content-Type: text/plain`, body `Hello`
- `GET /json`: `200`, `Content-Type: application/json`, body `{"message":"hello","value":123}`
- `GET /users/123?active=true`: `200`, `Content-Type: application/json`, body includes id `123` and active `true`
- `POST /echo`: `200`, `Content-Type: application/json`, echoes `{"name":"Arthur","message":"hello","value":123}`
- `POST /consume`: `204`, no body, request body consumed

## Build

Build and install the local `kiss-server` artifact first so the isolated benchmark app can resolve it:

```bash
mvn -B install -DskipTests
```

Build each benchmark app:

```bash
mvn -B -f benchmarks/apps/kiss-server-app/pom.xml package
mvn -B -f benchmarks/apps/undertow-app/pom.xml package
mvn -B -f benchmarks/apps/vertx-app/pom.xml package
```

Benchmark dependencies are isolated in the app POMs:

- `kiss-server-app`: local `io.github.arthurhoch:kiss-server`
- `undertow-app`: `io.undertow:undertow-core`
- `vertx-app`: `io.vertx:vertx-core`
- all apps: `maven-shade-plugin` to create runnable benchmark jars

The main `kiss-server` core POM is unchanged and still has zero production dependencies.

## Run Apps

Run one app at a time on the same machine, Java version, heap, and port:

```bash
KISS_MODE=worker PORT=8080 java -Xms512m -Xmx512m -jar benchmarks/apps/kiss-server-app/target/kiss-server-app.jar
KISS_MODE=direct PORT=8080 java -Xms512m -Xmx512m -jar benchmarks/apps/kiss-server-app/target/kiss-server-app.jar
KISS_MODE=virtual-threads PORT=8080 java -Xms512m -Xmx512m -jar benchmarks/apps/kiss-server-app/target/kiss-server-app.jar
PORT=8080 java -Xms512m -Xmx512m -jar benchmarks/apps/undertow-app/target/undertow-app.jar
PORT=8080 java -Xms512m -Xmx512m -jar benchmarks/apps/vertx-app/target/vertx-app.jar
```

The kiss-server app uses fast static routes for `/health`, `/hello`, and `/json` by default. Disable that with `KISS_FAST_STATIC=false` when measuring normal routes for those endpoints. Label results clearly.

Optional GC logging:

```bash
PORT=8080 java -Xms512m -Xmx512m -Xlog:gc*:file=gc.log:time,uptime,level,tags -jar benchmarks/apps/kiss-server-app/target/kiss-server-app.jar
```

Use the same JVM flags for every server.

## Run wrk

Install `wrk` and run the script from the repository root. The script performs a 10 second warmup before each measured run and then runs the required 30 second scenario with `--latency`.

```bash
BASE_URL=http://127.0.0.1:8080 OUT_DIR=benchmarks/results/kiss-server-nio-worker-fast-static ./benchmarks/scripts/run-all.sh kiss-server-nio-worker-fast-static
BASE_URL=http://127.0.0.1:8080 OUT_DIR=benchmarks/results/kiss-server-nio-direct-fast-static ./benchmarks/scripts/run-all.sh kiss-server-nio-direct-fast-static
BASE_URL=http://127.0.0.1:8080 OUT_DIR=benchmarks/results/kiss-server-nio-virtual-threads-jdk21-fast-static ./benchmarks/scripts/run-all.sh kiss-server-nio-virtual-threads-jdk21-fast-static
BASE_URL=http://127.0.0.1:8080 OUT_DIR=benchmarks/results/undertow ./benchmarks/scripts/run-all.sh undertow
BASE_URL=http://127.0.0.1:8080 OUT_DIR=benchmarks/results/vertx ./benchmarks/scripts/run-all.sh vertx
```

The script writes one result file per scenario under `OUT_DIR`.

Manual warmup example:

```bash
wrk --latency -t8 -c500 -d10s http://127.0.0.1:8080/health
```

## Scenarios

The script runs the same measured scenarios for every server:

- `GET /health` with `-t4 -c100 -d30s`
- `GET /health` with `-t8 -c500 -d30s`
- `GET /hello` with `-t8 -c500 -d30s`
- `GET /json` with `-t8 -c500 -d30s`
- `GET /users/123?active=true` with `-t8 -c500 -d30s`
- `POST /echo` with `-t8 -c500 -d30s`
- `POST /consume` with `-t8 -c500 -d30s`

All `wrk` commands use `--latency`.

## Fair Comparison Rules

Record these inputs with every benchmark run:

- commit hash
- OS and kernel
- CPU model and core count
- memory
- Java vendor and version
- JVM flags
- server name and app version
- benchmark command
- warmup duration
- measured duration
- concurrency and thread count
- whether the machine was otherwise idle

Use the same Java version, heap settings, machine, endpoint payloads, duration, concurrency, and benchmark tool for every server. Do not tune one server and leave the others at weak defaults. If you tune thread counts, socket options, allocator settings, or GC, record the exact setting and apply equivalent intent to all servers.

Do not mix kiss-server fast-static results with normal route claims. It is valid to report fast-static endpoint results when they are clearly labeled, but dynamic and POST scenarios must be reported separately.

## Metrics That Matter

Compare:

- requests/sec
- latency average
- latency p50, p90, and p99
- socket errors
- non-2xx/3xx responses
- CPU usage
- memory usage
- GC behavior

`wrk` reports requests/sec, average latency, latency distribution, socket errors, and non-2xx/3xx responses. Use OS tools such as `top`, `htop`, `vm_stat`, `jcmd`, Java Flight Recorder, or GC logs for CPU, memory, and GC behavior.

Localhost benchmarks are useful for repeatable development checks, but they are not final production truth. Real deployment results depend on NICs, kernel settings, TLS termination, proxies, payload distribution, client geography, CPU limits, container settings, and noisy neighbors.
