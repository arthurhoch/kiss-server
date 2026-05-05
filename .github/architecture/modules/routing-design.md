# Routing Design

Routing must stay simple and predictable.

## Matching Order

1. Fast exact routes.
2. Normal exact routes.
3. Dynamic routes.
4. 404 when no route matches.
5. 405 when the path exists for another method.

## Exact Routes

Use method-specific exact route maps for common paths:

```text
GET /health
POST /echo
```

Exact routes should be `HashMap` lookups when the full router is implemented.

Exact route matching ignores the query string. Fixed routes have priority over dynamic routes.

## Dynamic Routes

Dynamic route syntax:

```text
/users/{id}
```

Use a method-specific route trie for dynamic segments. Extract path params without regex.

Current dynamic matching scans path segments by index and avoids `String.split` or intermediate segment lists. Path parameter maps are allocated only after a dynamic route pattern is otherwise matching.

## Methods

Routes are method-specific. `GET /users/{id}` and `POST /users/{id}` are different routes.

## 404 and 405

If no route matches the path, return 404. If the path matches another method, return 405 with an `Allow` header later.
