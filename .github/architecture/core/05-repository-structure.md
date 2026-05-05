# Repository Structure

## Root

- `README.md` - project overview and quick examples.
- `AGENTS.md` - primary AI agent instructions.
- `CAVEMAN.md` - compact project summary.
- `CHANGELOG.md` - Keep a Changelog history.
- `SECURITY.md` - security policy.
- `CONTRIBUTING.md` - contribution rules.
- `LICENSE.txt` - Apache License 2.0.
- `pom.xml` - Maven build and release configuration.

## Source

- `src/main/java/io/github/arthurhoch/kiss/server` - facade and configuration.
- `http` - HTTP request, response, status, headers.
- `routing` - route registration and matching.
- `protocol/http11` - parser, connection, response writer.
- `runtime` - accept loop, limiter, executor, shutdown.
- `buffer` - bounded buffer helpers.
- `errors` - exception hierarchy.

## GitHub

- `.github/workflows` - CI, CodeQL, Pages, Maven Central release.
- `.github/architecture` - architecture, ADRs, and capsules.
- `.github/ALL_MARKDOWN.md` - Markdown index.

## Docs

`docs/` is the GitHub Pages source and must remain readable directly on GitHub.
