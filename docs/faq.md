---
layout: default
---

# FAQ

## Is the server engine implemented?

Yes. The initial blocking HTTP/1.1 socket engine is implemented for simple routes, `Content-Length` request bodies, keep-alive, connection close, parser limits, and safe default error responses. More hardening and features remain before a stable release.

## Why no production dependencies?

To keep the library small, inspectable, Native Image friendly, and easy to embed.

## Why no HTTP/2?

HTTP/2 adds binary framing, multiplexing, HPACK, stream state, flow control, and negotiation. KissServer starts with HTTP/1.1.

## How do I use virtual threads?

From JDK 21+ application code, pass `Executors.newVirtualThreadPerTaskExecutor()` through `ServerConfig.builder().executor(...)`.

## Does KissServer include JSON?

No. Use any JSON library from application code. `kiss-json` is a sibling library, not a dependency.

## Does KissServer terminate TLS?

Not in the initial core. Use Cloudflare, Nginx, Caddy, or a similar proxy.
