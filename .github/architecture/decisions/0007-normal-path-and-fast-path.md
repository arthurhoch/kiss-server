# ADR 0007: Normal Path and Fast Path

## Status

Accepted.

## Decision

KissServer has a normal handler path and an optional fast path for exact static routes.

## Reason

Normal business routes need request/context/response flexibility. Health and readiness endpoints benefit from prebuilt response bytes and lower allocation.

## Consequences

- Fast exact routes are checked before normal exact routes.
- The fast path must not complicate the normal public API.
