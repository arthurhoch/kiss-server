# Parser Design

The HTTP parser is byte-oriented.

## Forbidden

The parser must not use:

- regex;
- `String.split`;
- `Scanner`;
- `BufferedReader.readLine`;
- unbounded `StringBuilder` growth.

## Limits

The parser must enforce:

- max request line bytes;
- max header bytes;
- max header count;
- max body bytes.

## Request Line

Parse method, target, and version by scanning bytes for spaces and `CRLF`. Reject missing parts, extra junk, invalid method bytes, unsupported methods, and unsupported versions.

## Headers

Read header bytes until `CRLF CRLF`. Validate header names while scanning. Reject invalid names, folded headers, oversized headers, and malformed lines.

The implemented parser keeps a reusable byte buffer per connection. It fills that buffer with chunk reads from `InputStream`, scans by index for `CRLF`, and leaves unread bytes in place for the next keep-alive request.

## Body

Read exactly `Content-Length` bytes when present. Reject bodies over the configured maximum. Avoid growing buffers beyond the configured body limit.

Body bytes that have already arrived after the header terminator are copied before reading more from the socket. This prevents pipelined keep-alive bytes from being lost or consumed as part of the wrong request.

## Lazy Decoding

Decode strings only after byte validation. Future implementations may keep path and headers in byte slices until needed, but the code must stay understandable.

## Malformed Input Behavior

Malformed input maps to safe HTTP errors such as `400`, `413`, or `431`. Internal exceptions must not leak sensitive implementation details to clients.
