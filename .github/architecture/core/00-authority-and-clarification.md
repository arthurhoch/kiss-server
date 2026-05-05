# Authority and Clarification

## Authority

The authoritative project documents are:

1. `AGENTS.md`
2. `.github/architecture/index.md`
3. `.github/architecture/modules/*`
4. `docs/*`
5. `README.md`

If files conflict, prefer the more specific architecture module over the general overview. If a public example conflicts with an architecture rule, fix the example or document the change with an ADR.

## Clarification Rule

Future agents should not guess about protocol behavior, security limits, or public API changes. If a detail is missing, add a small architecture clarification before implementing code.

## Current Status

This repository currently contains the project foundation and compile-safe skeleton. The HTTP/1.1 server engine and parser are planned, not complete.
