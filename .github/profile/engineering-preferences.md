# Engineering Preferences

## Code

- Prefer boring Java over clever abstractions.
- Keep classes small and responsibilities clear.
- Prefer immutable configuration.
- Avoid global mutable state.
- Use explicit limits for all client-controlled data.
- Use standard library types before creating new abstractions.

## Tests

- Deterministic local tests first.
- Real socket integration tests for runtime behavior.
- Parser tests for every malformed input category.
- Benchmark code stays separate from normal CI.

## Documentation

- Keep docs short, direct, and actionable.
- Say "planned" when behavior is not implemented yet.
- Update README, docs, architecture, tests, and changelog together for public behavior changes.
