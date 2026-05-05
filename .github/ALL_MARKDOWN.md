# All Markdown Files

Manual index of every Markdown file in the KissServer repository and its purpose. Update this file whenever Markdown files are added, removed, or renamed.

## Reading Order for Agents

1. `AGENTS.md`
2. `CAVEMAN.md`
3. `README.md`
4. `docs/ai-usage.md`
5. `docs/tutorial-for-ai.md`
6. `docs/ai-performance-guide.md`
7. `.github/architecture/index.md`
8. Relevant architecture module docs
9. `.github/ALL_MARKDOWN.md`
10. Relevant user docs under `docs/`

## Root

| File | Purpose |
|------|---------|
| `README.md` | GitHub landing page, Maven snippet, examples, scope, docs links |
| `AGENTS.md` | Primary AI coding agent instructions |
| `CAVEMAN.md` | Compact low-token summary |
| `CHANGELOG.md` | Version history |
| `SECURITY.md` | Security policy |
| `CONTRIBUTING.md` | Contribution rules |

## `.github/`

| File | Purpose |
|------|---------|
| `.github/ALL_MARKDOWN.md` | This manual Markdown index |
| `.github/ai-rules.md` | Strict AI behavior rules |
| `.github/copilot-instructions.md` | Copilot-optimized instructions |
| `.github/logging-guidelines.md` | Logging and no-logging-dependency rules |
| `.github/profile/engineering-preferences.md` | Engineering preferences |

## Architecture Core

| File | Purpose |
|------|---------|
| `.github/architecture/index.md` | Architecture reading order |
| `.github/architecture/core/00-authority-and-clarification.md` | Document authority and clarification rules |
| `.github/architecture/core/01-system-purpose.md` | Project purpose |
| `.github/architecture/core/02-core-principles.md` | Core KISS principles |
| `.github/architecture/core/03-ai-behavior-rules.md` | AI behavior rules |
| `.github/architecture/core/04-code-generation-rules.md` | Code generation constraints |
| `.github/architecture/core/05-repository-structure.md` | Repository layout |
| `.github/architecture/core/06-assumptions-and-defaults.md` | Defaults and assumptions |
| `.github/architecture/core/07-task-execution-rules.md` | Task execution and completion rules |

## Architecture Kernel

| File | Purpose |
|------|---------|
| `.github/architecture/kernel/00-kernel-rules.md` | Non-negotiable project invariants |
| `.github/architecture/kernel/01-decision-boundaries.md` | ADR boundaries |
| `.github/architecture/kernel/02-ai-reasoning-model.md` | Reasoning priority model |

## Architecture Modules

| File | Purpose |
|------|---------|
| `.github/architecture/modules/http11-protocol.md` | HTTP/1.1 protocol scope |
| `.github/architecture/modules/socket-engine.md` | Socket engine design |
| `.github/architecture/modules/nio-engine.md` | Java 17 NIO engine design |
| `.github/architecture/modules/ai-usage.md` | AI usage and generation rules |
| `.github/architecture/modules/performance-strategy.md` | Performance strategy and benchmark claim rules |
| `.github/architecture/modules/parser-design.md` | Byte parser design |
| `.github/architecture/modules/routing-design.md` | Routing design |
| `.github/architecture/modules/request-response-context.md` | Request, response, context model |
| `.github/architecture/modules/normal-path-vs-fast-path.md` | Normal and fast execution paths |
| `.github/architecture/modules/fast-path.md` | Fast path contract and benchmark labeling |
| `.github/architecture/modules/executor-model.md` | Executor ownership and virtual-thread usage |
| `.github/architecture/modules/buffer-management.md` | Buffer pool and memory limits |
| `.github/architecture/modules/error-handling.md` | Error categories and mapping |
| `.github/architecture/modules/security-model.md` | Security limits and validation |
| `.github/architecture/modules/testing-strategy.md` | Test matrix |
| `.github/architecture/modules/benchmarking-strategy.md` | Benchmarking plan |
| `.github/architecture/modules/public-api-design.md` | Public API rules |
| `.github/architecture/modules/release-and-maven-central.md` | Release and Central publishing |
| `.github/architecture/modules/github-pages.md` | GitHub Pages docs |
| `.github/architecture/modules/dependency-policy.md` | Dependency policy |
| `.github/architecture/modules/backwards-compatibility.md` | Compatibility policy |
| `.github/architecture/modules/android-compatibility.md` | Android compatibility notes |
| `.github/architecture/modules/native-image.md` | GraalVM Native Image friendliness |

## Architecture Decisions

| File | Purpose |
|------|---------|
| `.github/architecture/decisions/0001-zero-production-dependencies.md` | Zero production dependencies |
| `.github/architecture/decisions/0002-http11-first.md` | HTTP/1.1 first |
| `.github/architecture/decisions/0003-jdk17-baseline.md` | JDK 17 baseline |
| `.github/architecture/decisions/0004-executor-service-injection.md` | ExecutorService injection |
| `.github/architecture/decisions/0005-no-servlet-api.md` | No Servlet API |
| `.github/architecture/decisions/0006-no-reflection-in-hot-path.md` | No hot-path reflection |
| `.github/architecture/decisions/0007-normal-path-and-fast-path.md` | Normal and fast paths |
| `.github/architecture/decisions/0008-nginx-caddy-cloudflare-for-tls-http2-http3.md` | External TLS and HTTP/2 or HTTP/3 |
| `.github/architecture/decisions/0009-graalvm-native-image-friendly.md` | Native Image friendly design |
| `.github/architecture/decisions/0010-single-java17-nio-engine.md` | Single Java 17 NIO engine |

## Architecture Capsules

| File | Purpose |
|------|---------|
| `.github/architecture/capsules/core-codegen-capsule.md` | Core codegen guidance |
| `.github/architecture/capsules/networking-capsule.md` | Networking implementation guidance |
| `.github/architecture/capsules/security-capsule.md` | Security implementation guidance |
| `.github/architecture/capsules/testing-capsule.md` | Testing guidance |
| `.github/architecture/capsules/release-capsule.md` | Release guidance |

## Benchmarks

| File | Purpose |
|------|---------|
| `benchmarks/README.md` | Benchmark app overview |
| `benchmarks/results/20260504T190706Z/summary.md` | Historical raw benchmark summary |

## Docs

| File | Purpose |
|------|---------|
| `docs/index.md` | GitHub Pages entry point |
| `docs/AI_QUICKSTART.md` | Short AI context quickstart |
| `docs/getting-started.md` | Installation and first routes |
| `docs/api.md` | API reference |
| `docs/configuration.md` | Configuration reference |
| `docs/examples.md` | Copyable examples |
| `docs/examples-ai-generated-app.md` | Complete small AI-generated app example |
| `docs/ai-usage.md` | AI coding agent usage guide |
| `docs/tutorial-for-ai.md` | Step-by-step tutorial for AI agents and humans |
| `docs/ai-performance-guide.md` | AI-focused performance guide |
| `docs/http11.md` | HTTP/1.1 protocol guide |
| `docs/nio-engine.md` | Java 17 NIO engine guide |
| `docs/performance.md` | Performance philosophy |
| `docs/performance-analysis.md` | Current performance bottlenecks, fixes, and future work |
| `docs/benchmarking.md` | Benchmarking plan |
| `docs/security.md` | Security design |
| `docs/error-handling.md` | Error handling guide |
| `docs/routing.md` | Routing guide |
| `docs/fast-path.md` | Fast path guide |
| `docs/executor-model.md` | Executor model guide |
| `docs/deployment.md` | Deployment guide |
| `docs/android.md` | Android notes |
| `docs/native-image.md` | Native Image notes |
| `docs/release.md` | Release process |
| `docs/maven-central.md` | Maven Central publishing |
| `docs/roadmap.md` | Implementation roadmap |
| `docs/faq.md` | FAQ |
| `docs/testing-report.md` | Current verification report |

## Prompt Docs

| File | Purpose |
|------|---------|
| `docs/prompts/generate-kiss-server-app.md` | Copy-paste prompt for generating a Java 17 kiss-server app |
| `docs/prompts/add-route.md` | Copy-paste prompt for adding a route correctly |
| `docs/prompts/optimize-kiss-server-app.md` | Copy-paste prompt for optimizing a kiss-server app |
| `docs/prompts/benchmark-kiss-server-app.md` | Copy-paste prompt for fair benchmarking |
