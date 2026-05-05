# Networking Capsule

Use this for socket engine, parser, writer, keep-alive, and timeout work.

## Required Reading

- `modules/http11-protocol.md`
- `modules/socket-engine.md`
- `modules/parser-design.md`
- `modules/security-model.md`

## Rules

- Byte-oriented parser.
- Enforce all configured limits.
- Handle timeouts and disconnects.
- Preserve executor ownership rules.
- Add real socket integration tests.
