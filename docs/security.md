# Security

KissServer is designed around explicit limits and zero production dependencies.

## Limits

- max request line bytes;
- max header bytes;
- max header count;
- max body bytes;
- max connections;
- read timeout;
- write timeout;
- idle timeout;
- max keep-alive requests.

## Validation

- Reject malformed request lines.
- Reject invalid header names.
- Reject oversized data.
- Validate response headers to prevent header injection.
- Do not trust `Content-Length`.
- Avoid uncontrolled buffer growth.
- Avoid path traversal in any future static file feature.

## Reporting

See [../SECURITY.md](../SECURITY.md).
