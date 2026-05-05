# ADR 0002: HTTP/1.1 First

## Status

Accepted.

## Decision

KissServer implements HTTP/1.1 first.

## Reason

HTTP/1.1 is enough for simple APIs, internal services, local tools, and proxy-behind deployments. HTTP/2 adds binary framing, multiplexing, HPACK, stream state, flow control, and negotiation complexity.

## Consequences

- HTTP/2 is out of scope.
- Production deployments should use Cloudflare, Nginx, Caddy, or similar for TLS and HTTP/2 or HTTP/3.
