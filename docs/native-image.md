---
layout: default
---

# GraalVM Native Image

KissServer is designed to be GraalVM Native Image friendly by default, but official support requires validation with an example application or CI workflow.

## Design Choices

- NIO-based HTTP/1.1 engine using explicit Java APIs.
- Explicit `ServerConfig` instead of runtime scanning.
- Zero production dependencies.
- No core reflection.
- No reflection in the hot path.
- No dynamic proxies.
- No annotation scanning.
- No classpath scanning.
- No `ServiceLoader` requirement for core behavior.
- No JNI or Unsafe.
- No generated classes.

These choices reduce the need for Native Image metadata.

## Consuming Applications Own Native Builds

Native compilation belongs to the consuming application. Application dependencies, resources, reflection needs, logging, and build plugins can change Native Image requirements.

Normal kiss-server usage should not require:

- `reflection-config.json`;
- `resource-config.json`;
- `proxy-config.json`.

That remains a design expectation until validated by a sample or CI build.

## Executor Notes

Pass an explicit executor from application code:

```java
ServerConfig config = ServerConfig.builder()
        .executor(executor)
        .build();
```

`ExecutorFactories.virtualThreadPerTaskOrCached()` uses reflection as an optional JVM convenience. Native Image users should prefer passing an explicit executor.

## Status

Do not claim official Native Image support until a native example app or CI workflow validates:

- build success;
- startup;
- request handling;
- shutdown;
- behavior with configured executor;
- docs for any application-level Native Image metadata.
