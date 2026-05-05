# Executor Model

KissServer separates network I/O from normal application handler execution.

## Selector And Event Loop

The NIO engine uses a selector thread for network work:

- accepting connections;
- reading socket bytes;
- parsing HTTP/1.1 requests;
- writing queued responses;
- tracking connection timeouts and keep-alive state.

The selector thread should stay fast. Blocking work belongs on an executor through the normal route path.

## Normal Handlers Use ExecutorService

`HandlerExecutionMode.WORKER` is the default. In this mode normal handlers run on the configured `ExecutorService`:

```java
ServerConfig config = ServerConfig.builder()
        .handlerExecutionMode(HandlerExecutionMode.WORKER)
        .build();
```

Use this for normal application behavior, especially handlers that might block or take measurable CPU time.

## Fast Path Can Avoid The Executor

Exact fixed `GET` routes registered with `fastGet` can be served directly by the NIO engine:

```java
server.fastGet("/health", FastResponses.text("OK"));
```

This is appropriate only for fixed responses. Do not hide slow work in the fast path.

## Java 17 Default Executor

KissServer compiles on Java 17. Main source does not directly call JDK 21-only APIs.

If no executor is provided, KissServer creates an internal default executor. That executor is owned by the server and is shut down when the server stops.

## User-Provided Executor

Applications may pass an executor:

```java
ExecutorService executor = Executors.newFixedThreadPool(
        Math.max(4, Runtime.getRuntime().availableProcessors())
);

ServerConfig config = ServerConfig.builder()
        .port(8080)
        .executor(executor)
        .build();
```

If the application provides the executor, KissServer does not shut it down automatically. The application owns its lifecycle.

## Ownership Rules

- User-provided executor: application owns shutdown.
- KissServer-created executor: KissServer owns shutdown.
- Benchmark-created virtual-thread executor: benchmark application owns shutdown.

## JDK 21 Virtual Threads From Application Code Only

JDK 21+ applications may pass:

```java
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

ServerConfig config = ServerConfig.builder()
        .executor(executor)
        .build();
```

This is optional application code. It must not become required library usage and must not be called directly from kiss-server main source.

Benchmark reports must label this profile as `kiss-server on JDK 21 with virtual-thread executor`.

## Direct Mode

`DIRECT` runs normal handlers on the selector thread:

```java
ServerConfig config = ServerConfig.builder()
        .handlerExecutionMode(HandlerExecutionMode.DIRECT)
        .build();
```

Use `DIRECT` only for controlled, non-blocking, fast handlers. Do not use it for database calls, file I/O, network calls, sleeps, synchronous logging, or slow CPU work.

## Android Implications

Android compatibility is a design goal pending validation. Keep application examples on Java 17 APIs by default. Do not require virtual threads for Android. Android lifecycle, background execution, battery optimization, network changes, and port accessibility must be handled by the application.

## Blocking Handler Implications

Blocking handlers can exhaust executor threads or increase latency. For blocking-heavy apps:

- use `WORKER` mode;
- configure an executor explicitly;
- set downstream timeouts;
- avoid global locks;
- consider a JDK 21 virtual-thread executor in application code after measuring.

## Recommended Executor Choices

| Workload | Java 17 choice | Notes |
|----------|----------------|-------|
| CPU-small handlers | fixed pool near CPU count | predictable resource use |
| Mixed blocking handlers | cached pool or tuned fixed pool | measure queueing and memory |
| Many blocking calls on JDK 21 | virtual-thread executor | optional application code |
| Fixed static health/version | fast path | no normal executor work |

Always benchmark with the same executor settings you plan to deploy.
