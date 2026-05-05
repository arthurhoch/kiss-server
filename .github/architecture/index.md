# Architecture Index

This directory contains the authoritative architecture documentation for KissServer.

## Reading Order

1. [core/00-authority-and-clarification.md](core/00-authority-and-clarification.md)
2. [core/01-system-purpose.md](core/01-system-purpose.md)
3. [core/02-core-principles.md](core/02-core-principles.md)
4. [core/03-ai-behavior-rules.md](core/03-ai-behavior-rules.md)
5. [core/04-code-generation-rules.md](core/04-code-generation-rules.md)
6. [core/05-repository-structure.md](core/05-repository-structure.md)
7. [core/06-assumptions-and-defaults.md](core/06-assumptions-and-defaults.md)
8. [core/07-task-execution-rules.md](core/07-task-execution-rules.md)
9. [kernel/00-kernel-rules.md](kernel/00-kernel-rules.md)
10. [kernel/01-decision-boundaries.md](kernel/01-decision-boundaries.md)
11. [kernel/02-ai-reasoning-model.md](kernel/02-ai-reasoning-model.md)

## Module Docs

- [HTTP/1.1 Protocol](modules/http11-protocol.md)
- [Socket Engine](modules/socket-engine.md)
- [NIO Engine](modules/nio-engine.md)
- [AI Usage](modules/ai-usage.md)
- [Performance Strategy](modules/performance-strategy.md)
- [Parser Design](modules/parser-design.md)
- [Routing Design](modules/routing-design.md)
- [Request/Response/Context](modules/request-response-context.md)
- [Normal Path vs Fast Path](modules/normal-path-vs-fast-path.md)
- [Fast Path](modules/fast-path.md)
- [Executor Model](modules/executor-model.md)
- [Buffer Management](modules/buffer-management.md)
- [Error Handling](modules/error-handling.md)
- [Security Model](modules/security-model.md)
- [Testing Strategy](modules/testing-strategy.md)
- [Benchmarking Strategy](modules/benchmarking-strategy.md)
- [Public API Design](modules/public-api-design.md)
- [Release and Maven Central](modules/release-and-maven-central.md)
- [GitHub Pages](modules/github-pages.md)
- [Dependency Policy](modules/dependency-policy.md)
- [Backwards Compatibility](modules/backwards-compatibility.md)
- [Android Compatibility](modules/android-compatibility.md)
- [Native Image](modules/native-image.md)

## Decisions

Read the ADRs in [decisions/](decisions/) before changing project scope.

## Capsules

Capsules in [capsules/](capsules/) provide focused implementation instructions for AI agents.

## Maintenance

When adding, removing, or renaming Markdown files:

1. Update this index if the file is architecture-related.
2. Update `.github/ALL_MARKDOWN.md`.
3. Update docs links if the change affects public documentation.
