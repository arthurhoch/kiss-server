# AI Reasoning Model

AI agents should reason from constraints to code.

## Order

1. Public API contract.
2. Protocol correctness.
3. Security and limits.
4. Simplicity and maintainability.
5. Performance.

Performance matters, but it cannot override protocol correctness or input safety.

## Preferred Pattern

- Start with a small exact behavior.
- Add tests for valid, malformed, and boundary input.
- Keep the implementation readable.
- Document any trade-off.
