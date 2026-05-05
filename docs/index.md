---
layout: default
title: KissServer
---

# KissServer Documentation

A tiny, zero-dependency Java 17+ HTTP/1.1 socket server library.

Status: initial HTTP/1.1 core is ready for the first `0.1.0` release.

## Maven

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
