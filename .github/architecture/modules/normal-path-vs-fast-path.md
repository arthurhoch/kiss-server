# Normal Path vs Fast Path

KissServer has two internal execution paths.

## Normal Path

The normal path is the default for business routes:

- `Request`;
- `Context`;
- `Handler`;
- `Response`.

It is flexible and readable.

## Fast Path

The fast path is for exact static endpoints:

- exact route only;
- `DirectHandler`;
- prebuilt `byte[]` response;
- per-request `Connection` header handling by the engine;
- minimal allocation;
- fixed JSON is allowed through prebuilt bytes such as `FastResponses.json`;
- no reflection;
- no unnecessary `Request`, `Context`, or `Response` creation.

Good candidates:

- `/health`;
- `/ready`;
- `/version`;
- `/ping`;
- `robots.txt`.

## Public API Rule

Do not expose complex APIs because of the fast path. Fast path is optional and must not distort the normal route API.

## Router Rule

Check fast exact routes first, then normal exact routes, then dynamic routes.

Benchmark reports must separate normal handler results from fast path results and must label fast-static endpoint results clearly.
