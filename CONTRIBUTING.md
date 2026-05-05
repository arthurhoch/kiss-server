# Contributing

## Before You Start

Read:

1. `AGENTS.md`
2. `.github/ALL_MARKDOWN.md`
3. `.github/architecture/index.md`
4. The architecture module for the area you are changing

## Build

```bash
mvn -B verify
```

Run this before claiming work is complete.

## Rules

- Keep zero production dependencies.
- Do not add frameworks.
- Preserve Java 17 compatibility.
- Add or update tests for behavior changes.
- Update docs for public behavior changes.
- Update README examples for public API changes.
- Update `CHANGELOG.md` under `Unreleased`.
- Do not commit `target/`, IDE files, logs, `.DS_Store`, local env files, or generated build output.
- Follow `AGENTS.md` and the architecture docs.

## Dependency Changes

Any production dependency requires:

1. an ADR in `.github/architecture/decisions/`;
2. an update to `.github/architecture/modules/dependency-policy.md`;
3. an update to docs if public behavior changes;
4. tests proving the dependency is necessary.

The default answer should be no.
