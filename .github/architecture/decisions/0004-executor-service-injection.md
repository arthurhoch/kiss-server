# ADR 0004: ExecutorService Injection

## Status

Accepted.

## Decision

KissServer accepts a user-provided `ExecutorService`.

## Reason

Applications should own their threading model. This allows fixed pools, cached pools, custom instrumented executors, and JDK 21 virtual threads without raising the Java baseline.

## Consequences

- User-provided executors are not shut down automatically.
- KissServer-created executors are owned and shut down by the server.
