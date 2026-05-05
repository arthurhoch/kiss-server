# Request, Response, and Context

## Request

`Request` represents parsed HTTP input:

- method;
- path;
- query string;
- headers;
- body bytes.

It should not expose mutable internal buffers.

Body bytes remain byte-based until a handler asks for `bodyAsString()`. UTF-8 body string decoding is cached per request.

## Context

`Context` is the normal handler convenience object. It exposes:

- request;
- path params;
- body helpers;
- response helpers such as `text(...)`.

Context must not become a dependency injection container.

## Response

`Response` represents status, headers, and body bytes. Response header names and values must be validated to avoid header injection.

Response construction validates headers. The HTTP/1.1 writer computes `Content-Length` from the stored body and does not trust caller-supplied length headers.

## Future Query Params

Query parsing can be added as a small helper. It must avoid uncontrolled allocation and must document percent-decoding behavior.
