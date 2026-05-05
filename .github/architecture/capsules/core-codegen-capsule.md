# Core Codegen Capsule

Use this for public API and shared model changes.

## Rules

- Read `AGENTS.md` and the relevant architecture module.
- Keep Java 17 compatibility.
- Keep public API small.
- Add Javadocs where they clarify public behavior.
- Update README, docs, tests, and changelog for public changes.
- Run `mvn -B verify`.

## Avoid

- New production dependencies.
- Framework patterns.
- Hidden global state.
- Public internal implementation classes.
