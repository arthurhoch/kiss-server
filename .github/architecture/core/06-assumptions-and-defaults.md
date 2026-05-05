# Assumptions and Defaults

## Runtime Defaults

- Host: `0.0.0.0`.
- Port: `8080`.
- Keep-alive: enabled.
- Max connections: `10_000`.
- Max request line bytes: `8 KiB`.
- Max header bytes: `16 KiB`.
- Max body bytes: `10 MiB`.
- Read timeout: `5s`.
- Write timeout: `10s`.
- Idle timeout: `30s`.
- Max keep-alive requests: `1_000`.
- Buffer size: `16 KiB`.
- Buffer pool size: `2_048`.

Defaults may change before 1.0 if tests or benchmarks show a better trade-off.

## Deployment Assumption

KissServer speaks HTTP/1.1. Production TLS and HTTP/2 or HTTP/3 should usually be handled by Cloudflare, Nginx, Caddy, or a similar proxy.
