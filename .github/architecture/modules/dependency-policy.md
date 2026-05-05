# Dependency Policy

## Production

Zero production dependencies. The Java standard library is the only runtime dependency.

## Test

JUnit Jupiter is allowed for tests. Additional test dependencies require a clear reason and documentation.

## Build

Maven plugins for compiler, surefire, sources, Javadocs, GPG, Central Portal publishing, and optional security scanning are allowed.

## New Dependency Rule

Any production dependency requires:

1. ADR;
2. security review;
3. update to this document;
4. README/docs update if public behavior changes;
5. Dependabot compatibility.

Avoid dependency creep.
