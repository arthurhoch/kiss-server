# Executor Model

## Java Baseline

KissServer compiles with Java 17. Main source code must not directly call JDK 21-only APIs.

## Selector Responsibility

The selector/event-loop handles network I/O: accept, read, parse readiness, write readiness, timeout state, and keep-alive state. It must remain non-blocking.

## Normal Handler Execution

`HandlerExecutionMode.WORKER` is the safe default. Normal route handlers run on the configured `ExecutorService`.

```java
ServerConfig config = ServerConfig.builder()
        .handlerExecutionMode(HandlerExecutionMode.WORKER)
        .build();
```

## User-Provided Executor

Applications may pass an `ExecutorService`:

```java
ExecutorService executor = Executors.newFixedThreadPool(8);

ServerConfig config = ServerConfig.builder()
        .executor(executor)
        .build();
```

If the user provides the executor, KissServer must not shut it down automatically.

## Owned Executor

If KissServer creates the executor, KissServer owns it and must shut it down on stop.

## Fast Path

Fast exact static routes can avoid executor dispatch because they use prebuilt response bytes. This is valid only for fixed, non-blocking, request-independent responses.

## Direct Mode

`HandlerExecutionMode.DIRECT` runs normal handlers on the selector thread. It is advanced and must only be used for handlers that never block, sleep, perform file I/O, call databases, call networks, or perform slow work.

## JDK 21 Virtual Threads

JDK 21+ users may pass:

```java
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
```

This belongs in application code. It must not be required by the library.

Benchmark reports must label this profile as `kiss-server on JDK 21 with virtual-thread executor`. Android and Java 17 examples must not require virtual threads.

## Optional Helper

A Java 17-compatible reflection helper may offer `virtualThreadPerTaskOrCached()`. It is a JVM convenience and not the recommended Native Image path.
