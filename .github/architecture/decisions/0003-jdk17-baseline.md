# ADR 0003: JDK 17 Baseline

## Status

Accepted.

## Decision

KissServer compiles with Java 17 using Maven compiler release 17.

## Reason

Java 17 is a stable LTS baseline and keeps the library usable in more environments.

## Consequences

- No Java 21-only API calls in main source.
- Virtual threads are available only through user-provided executors or optional reflection helpers.
