# Error Handling

## Categories

Handle:

- bind errors;
- socket timeouts;
- unexpected disconnects;
- malformed requests;
- oversized request line;
- oversized headers;
- oversized body;
- unsupported methods;
- unsupported HTTP versions;
- broken pipe and write failures;
- executor rejection;
- shutdown during accept.

## Client Errors

Malformed client input maps to safe HTTP status codes:

- `400 Bad Request`;
- `413 Payload Too Large`;
- `431 Request Header Fields Too Large`;
- `405 Method Not Allowed`;
- `404 Not Found`.

## Server Errors

Unexpected handler failures should map to `500 Internal Server Error` unless a custom error handler maps them differently.

## Security

Do not leak stack traces or internal file paths to clients. Keep detailed diagnostics in exceptions/tests, not default responses.
