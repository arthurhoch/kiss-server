# Security Policy

## Supported Versions

| Version | Supported |
| ------- | --------- |
| 0.1.x   | Supported |

KissServer `0.1.0` is published to Maven Central. Security support applies to released `0.1.x` artifacts and the active development line. Before 1.0, APIs and behavior may change.

## Reporting a Vulnerability

Use GitHub Security Advisories when possible. Do not open a public issue for an undisclosed vulnerability.

Include:

- affected version or commit;
- reproduction steps;
- expected and actual behavior;
- impact and suggested mitigation if known.

## Security Design Principles

- Zero production dependencies.
- Byte-oriented parser with explicit limits.
- No framework runtime.
- No annotation scanning.
- No reflection in the hot path.
- No classpath scanning, dynamic proxies, JNI, Unsafe, or generated classes.
- Validate request lines, headers, body sizes, and response headers.
- Avoid path traversal in any future file-serving feature.
- Return safe errors by default.

## Dependency Policy

KissServer has zero production dependencies. JUnit Jupiter is test-scope only. Build plugins and security tools do not ship in the published artifact.

Adding any production dependency requires an ADR, documentation update, and security review.

## Security Scanning

Normal build:

```bash
mvn -B verify
```

Optional dependency scanning:

```bash
mvn -Psecurity verify
```

CodeQL and Dependabot run in GitHub Actions. The security profile is separate so normal CI stays fast and secret-free.
