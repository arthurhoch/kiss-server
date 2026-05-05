# Deployment

Recommended production model:

```text
client/browser
  -> Cloudflare/Nginx/Caddy using TLS + HTTP/2 or HTTP/3
      -> kiss-server using HTTP/1.1 locally
```

## Why

KissServer focuses on a tiny HTTP/1.1 application server. TLS certificates, HTTP/2, HTTP/3, compression policy, edge caching, and advanced proxy behavior are better handled by mature edge tools.

## Local and Embedded

For local tools, tests, and embedded services, KissServer can bind directly to loopback or a configured interface.
