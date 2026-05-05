# ADR 0005: No Servlet API

## Status

Accepted.

## Decision

KissServer does not use or expose the Servlet API.

## Reason

Servlet APIs bring a container model and dependency surface that conflict with the tiny embedded server goal.

## Consequences

- Request, response, context, routing, and lifecycle types are KissServer-specific.
- Servlet compatibility is not a v1 goal.
