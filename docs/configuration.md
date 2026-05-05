# Configuration

`ServerConfig` is immutable and created with a builder.

```java
ServerConfig config = ServerConfig.builder()
        .host("0.0.0.0")
        .port(8080)
        .maxConnections(10_000)
        .maxHeaderBytes(16 * 1024)
        .maxRequestLineBytes(8 * 1024)
        .maxBodyBytes(10L * 1024 * 1024)
        .readTimeoutMillis(5_000)
        .writeTimeoutMillis(10_000)
        .idleTimeoutMillis(30_000)
        .keepAlive(true)
        .maxKeepAliveRequests(1_000)
        .bufferSize(16 * 1024)
        .bufferPoolSize(2_048)
        .handlerExecutionMode(HandlerExecutionMode.WORKER)
        .build();
```

## Defaults

| Setting | Default |
|---------|---------|
| Host | `0.0.0.0` |
| Port | `8080` |
| Max connections | `10_000` |
| Max request line bytes | `8 KiB` |
| Max header bytes | `16 KiB` |
| Max body bytes | `10 MiB` |
| Read timeout | `5s` |
| Write timeout | `10s` |
| Idle timeout | `30s` |
| Keep-alive | enabled |
| Max keep-alive requests | `1_000` |
| Buffer size | `16 KiB` |
| Buffer pool size | `2_048` |
| Handler execution mode | `WORKER` |

## Executor

```java
ExecutorService executor = Executors.newCachedThreadPool();

ServerConfig config = ServerConfig.builder()
        .executor(executor)
        .build();
```

User-provided executors are not shut down automatically. KissServer-owned executors are shut down on stop.

## Handler Execution Mode

`WORKER` is the safe default. `DIRECT` runs normal handlers on the selector thread and must be reserved for non-blocking, fast handlers.
