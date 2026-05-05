# Kernel Rules

Kernel rules are the project invariants.

1. Zero production dependencies.
2. Java 17 baseline.
3. HTTP/1.1 first.
4. Socket engine, not Servlet.
5. No frameworks.
6. No reflection in the hot path.
7. Bounded input and buffers.
8. Explicit executor ownership.
9. Documentation and tests move with behavior.
10. Simplicity wins when trade-offs are otherwise equal.
