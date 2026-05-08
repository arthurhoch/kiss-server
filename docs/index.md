---
layout: default
title: KissServer
---

# KissServer Documentation

Tiny zero-dependency Java 17+ HTTP/1.1 server library for simple REST-style applications.

Part of the KISS Java Libraries family.

Status: latest stable release is `0.1.0`.

## Install

```xml
<dependency>
  <groupId>io.github.arthurhoch</groupId>
  <artifactId>kiss-server</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Quick Example

```java
KissServer server = KissServer.create();
server.get("/health", ctx -> ctx.text("OK"));
server.start(8080).await();
```

## Core Features

- Java 17-compatible NIO HTTP/1.1 engine.
- Simple `get`, `post`, exact route, dynamic route, request, context, and response APIs.
- Fast path for exact static/simple `GET` responses.
- Normal route path with configurable handler execution.
- Bounded parser limits, safe default errors, and predictable shutdown.
- Optional application-provided JDK 21 virtual-thread executor usage without requiring Java 21 in the main artifact.

## Guides

| Document | Description |
|----------|-------------|
| [Getting Started](getting-started.md) | Installation and first routes |
| [API](api.md) | Public API reference |
| [Configuration](configuration.md) | ServerConfig options |
| [Examples](examples.md) | Copyable usage examples |
| [AI Quickstart](AI_QUICKSTART.md) | Short context for AI tools |
| [Using kiss-server with AI coding agents](ai-usage.md) | AI generation and change guide |
| [Tutorial for AI agents and humans](tutorial-for-ai.md) | Step-by-step Java 17 app tutorial |
| [AI Performance Guide](ai-performance-guide.md) | Performance-focused AI guidance |
| [Example AI-Generated App](examples-ai-generated-app.md) | Complete small app pattern |
| [HTTP/1.1](http11.md) | Protocol scope |
| [NIO Engine](nio-engine.md) | Selector-based runtime |
| [Routing](routing.md) | Exact and dynamic routes |
| [Fast Path](fast-path.md) | Direct exact-route responses |
| [Executor Model](executor-model.md) | User and owned executors |
| [Performance](performance.md) | Performance philosophy |
| [Performance Analysis](performance-analysis.md) | Current bottlenecks, fixes, and future work |
| [Benchmarking](benchmarking.md) | Benchmark plan and commands |
| [Safe Code Cleanup](code-cleanup.md) | Coverage, quality checks, and deletion policy |
| [Security](security.md) | Limits and safe defaults |
| [Error Handling](error-handling.md) | Error categories and mapping |
| [Deployment](deployment.md) | Reverse proxy recommendation |
| [Android](android.md) | Android compatibility notes |
| [Native Image](native-image.md) | GraalVM Native Image friendliness |
| [Roadmap](roadmap.md) | Planned implementation phases |
| [FAQ](faq.md) | Common questions |
| [Release](release.md) | Release process |
| [Maven Central](maven-central.md) | Publishing setup |
| [Testing Report](testing-report.md) | Current verification results and known limits |

## Related KISS Projects

These libraries are independent, zero-dependency Java 17+ projects. Use only the modules you need.

| Project | Purpose |
|---|---|
| [kiss-json](https://github.com/arthurhoch/kiss-json) | Field-based JSON serialization and deserialization. |
| [kiss-requests](https://github.com/arthurhoch/kiss-requests) | Simple HTTP client built on Java HttpClient. |
| [kiss-server](https://github.com/arthurhoch/kiss-server) | Small HTTP/1.1 server for simple REST-style applications. |
| [kiss-config](https://github.com/arthurhoch/kiss-config) | Configuration loading from properties, .env files, system properties, and environment variables. |
| [kiss-binary](https://github.com/arthurhoch/kiss-binary) | Explicit binary IO for primitive binary formats. |

## Links

- [GitHub](https://github.com/arthurhoch/kiss-server)
- [Maven Central](https://central.sonatype.com/artifact/io.github.arthurhoch/kiss-server)
- [Changelog](https://github.com/arthurhoch/kiss-server/blob/main/CHANGELOG.md)
- [Security Policy](https://github.com/arthurhoch/kiss-server/blob/main/SECURITY.md)
