# ADR 0001: Zero Production Dependencies

## Status

Accepted.

## Decision

KissServer has zero production dependencies. The Java standard library is the runtime dependency.

## Reason

The library must stay small, inspectable, Native Image friendly, and easy to embed. Dependencies increase API surface, transitive vulnerability risk, and operational complexity.

## Consequences

- JUnit is allowed only in test scope.
- Build plugins are allowed because they do not ship in the artifact.
- Any future production dependency requires a new ADR and dependency policy update.
