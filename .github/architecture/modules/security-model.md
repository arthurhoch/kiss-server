# Security Model

## Input Limits

All client-controlled input must have limits:

- request line bytes;
- header bytes;
- header count;
- body bytes;
- connections;
- timeouts;
- keep-alive requests.

## Validation

- Reject malformed request lines.
- Reject invalid header names.
- Reject header values that would inject response headers.
- Validate `Content-Length`.
- Normalize paths before any future static file serving.

## Do Not Trust Clients

Do not trust `Content-Length`, paths, headers, methods, or connection state. Validate before using.

## Path Traversal

Static file serving is future scope. If added, it must reject traversal and test symlink/path normalization behavior.

## DoS Considerations

Use timeouts, max connections, bounded buffers, keep-alive limits, and predictable parser work per byte.
