# Core Principles

## KISS

KISS means:

- keep it simple;
- keep it short and simple;
- avoid unnecessary abstractions;
- avoid framework-like complexity;
- avoid magic and hidden behavior;
- prefer explicit, readable code;
- every feature must justify its complexity.

## Engineering Principles

- Zero production dependencies.
- Java 17 baseline.
- Explicit public API.
- Readable implementation.
- Predictable performance.
- Minimal allocation in hot paths.
- Bounded memory growth.
- Maintainable tests and docs.
- Native Image friendly by default.

## Trade-Off Rule

Choose boring engineering over clever code. If an optimization makes the code hard to inspect, document the measured reason and keep the public API simple.
