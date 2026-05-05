# ADR 0006: No Reflection in Hot Path

## Status

Accepted.

## Decision

Core request parsing, routing, handling, and writing must not depend on reflection in the hot path.

## Reason

Reflection makes behavior harder to inspect, can hurt performance, and complicates Native Image builds.

## Consequences

- No annotation scanning for routes.
- No dependency injection container.
- Optional reflection helpers must be outside hot paths and documented.
