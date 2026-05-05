# ADR 0009: GraalVM Native Image Friendly

## Status

Accepted.

## Decision

KissServer is designed to be Native Image friendly by default, without becoming GraalVM-dependent.

## Reason

Zero dependencies, explicit configuration, no classpath scanning, no dynamic proxies, and no hot-path reflection make the library easier to compile into native applications.

## Consequences

- Native compilation is owned by consuming applications.
- Do not claim official support until validated.
- Future reflective or resource-based features must be optional, isolated, and documented.
