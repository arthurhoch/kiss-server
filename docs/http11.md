# HTTP/1.1

KissServer is HTTP/1.1 first.

## Implemented Core

- request line parsing;
- headers and CRLF handling;
- `Content-Length` request bodies;
- keep-alive by default;
- `Connection: close`;
- status line and response headers;
- response `Content-Length`;
- HEAD semantics;
- normal OPTIONS routes.

Malformed input maps to safe `400`, `413`, or `431` responses where practical, then the connection is closed.

The NIO parser preserves unread bytes so partial requests and multiple requests in one socket write can be handled in order.

## Out of Scope Initially

- HTTP/2;
- TLS termination;
- WebSocket;
- Servlet API;
- chunked request bodies unless explicitly added later;
- static file serving.

## Production Model

Use an edge/proxy for TLS and HTTP/2 or HTTP/3:

```text
client/browser
  -> Cloudflare/Nginx/Caddy using TLS + HTTP/2 or HTTP/3
      -> kiss-server using HTTP/1.1 locally
```
