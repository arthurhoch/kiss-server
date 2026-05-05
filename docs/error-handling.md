# Error Handling

## Error Categories

- bind errors;
- socket timeouts;
- unexpected disconnects;
- malformed requests;
- oversized headers;
- oversized body;
- unsupported methods;
- unsupported HTTP version;
- broken pipe/write failures;
- executor rejection;
- shutdown while accepting connections.

## Mapping

Expected mappings:

| Situation | Response |
|-----------|----------|
| malformed request | 400 |
| oversized body | 413 |
| oversized headers | 431 |
| no matching route | 404 |
| path exists for another method | 405 |
| handler failure | 500 |

Default client responses should not expose stack traces.

Custom error handler API is not implemented yet. Handler exceptions currently map to a safe `500 Internal Server Error` response.
