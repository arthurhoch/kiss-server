# Logging Guidelines

KissServer must not require a logging dependency.

## Rules

- Do not add SLF4J, Logback, Log4j, JUL wrappers, or observability dependencies.
- Core code should surface errors through exceptions, return values, and documented hooks.
- If future logging hooks are needed, they must be explicit Java interfaces and require an ADR.
- Never log request bodies by default.
- Never log secrets, authorization headers, cookies, or client-provided data without caller control.
- Parser and runtime errors should be debuggable through tests and exception messages.

## Future Direction

Applications can integrate their own logging around handlers and lifecycle callbacks. KissServer should not own application logging policy.
