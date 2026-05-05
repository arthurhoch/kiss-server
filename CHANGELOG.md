# Changelog

All notable changes to KissServer will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2026-05-04

### Added

- Initial release of `io.github.arthurhoch:kiss-server`.
- Zero-production-dependency Java 17 HTTP/1.1 server API.
- Java 17 NIO HTTP/1.1 engine with bounded parser limits, keep-alive, request body handling, routing, response writing, safe default errors, and predictable shutdown.
- Normal route path for business handlers and fast path for exact static `GET` responses.
- JUnit test coverage for parser, response writer, routing, buffer pool, request/response/context, executor ownership, and real socket integration behavior.
- Documentation for humans and AI coding agents, including performance, benchmarking, executor model, Android notes, Native Image notes, and release process.
- Isolated benchmark apps and raw benchmark result documentation using kiss-server, Undertow, and Vert.x reference apps.
- GitHub workflows for CI, CodeQL, GitHub Pages, and Maven Central publishing readiness.
- Maven Central release profile with sources jar, Javadocs jar, GPG signing, and Central Portal publishing plugin.

[0.1.0]: https://github.com/arthurhoch/kiss-server/releases/tag/v0.1.0
