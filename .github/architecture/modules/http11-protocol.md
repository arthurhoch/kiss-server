# HTTP/1.1 Protocol

KissServer is HTTP/1.1 first.

## Request Line

The parser must read:

```text
METHOD SP request-target SP HTTP/1.1 CRLF
```

Reject malformed request lines, empty methods, unsupported methods, unsupported versions, missing spaces, and request lines over the configured byte limit.

## Headers

Headers are ASCII names with token characters, followed by `:`, optional whitespace, a value, and `CRLF`. Header parsing must:

- enforce total header byte limit;
- enforce max header count;
- reject invalid header names;
- reject malformed lines;
- preserve enough information for routing and body handling.

## CRLF

HTTP line endings are `\r\n`. The parser should not accept ambiguous input that could hide request smuggling behavior.

## Content-Length and Body

For initial core, request bodies are read using `Content-Length`. The parser must reject invalid, negative, duplicate-conflicting, or oversized content lengths. Do not trust client-provided values beyond configured limits.

## Keep-Alive

HTTP/1.1 defaults to persistent connections. KissServer should keep the connection alive unless:

- request has `Connection: close`;
- response requires close;
- request is malformed;
- limits are exceeded;
- max keep-alive requests is reached;
- idle timeout expires;
- server is shutting down.

## Response

Responses must include:

- status line: `HTTP/1.1 200 OK`;
- safe headers;
- `Content-Length` when a body is sent;
- no header injection.

## HEAD

HEAD should produce the same headers as GET but no body bytes.

## OPTIONS and CORS

OPTIONS should be supported as a normal route. CORS helpers are not core v1 behavior unless explicitly added later.

## Initially Out of Scope

- HTTP/2.
- TLS.
- WebSocket.
- Chunked request bodies unless explicitly implemented later.
- Multipart parsing.
- Static file serving.

HTTP/2 is out of scope because it requires binary framing, multiplexing, HPACK, stream state, flow control, protocol negotiation, and much more implementation complexity.
