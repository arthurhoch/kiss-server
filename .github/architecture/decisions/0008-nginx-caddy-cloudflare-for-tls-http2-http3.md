# ADR 0008: External TLS and HTTP/2 or HTTP/3

## Status

Accepted.

## Decision

KissServer should normally run behind Cloudflare, Nginx, Caddy, or a similar proxy for TLS and HTTP/2 or HTTP/3.

## Reason

TLS termination, certificate management, HTTP/2, and HTTP/3 are large operational responsibilities. Mature edge/proxy tools already solve them well.

## Consequences

- KissServer core remains HTTP/1.1.
- TLS implementation is out of initial scope.
