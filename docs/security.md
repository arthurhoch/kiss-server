---
layout: default
---

# Security

KissServer is designed around explicit limits and zero production dependencies.

## Limits

- max request line bytes;
- max header bytes;
- max header count;
- max body bytes;
- max connections;
- read timeout;
- write timeout;
- idle timeout;
- max keep-alive requests.

## Validation

- Reject malformed request lines.
- Reject invalid header names.
- Reject oversized data.
- Validate response headers to prevent header injection.
- Do not trust `Content-Length`.
- Avoid uncontrolled buffer growth.
- Avoid path traversal in any future static file feature.

## Reporting

See [../SECURITY.md](../SECURITY.md).

## Quality And Coverage

Normal CI runs the fast Maven build:

```bash
mvn -B clean verify
```

JaCoCo coverage is generated during `verify`:

```text
target/site/jacoco/jacoco.xml
target/site/jacoco/index.html
```

Optional local security and static-analysis checks are:

```bash
mvn -Psecurity verify
mvn -Pspotbugs verify
```

CodeQL, Dependency Review, Dependabot, and OpenSSF Scorecard are documented in [security-hardening.md](security-hardening.md), including the required GitHub repository settings.

Use [code-cleanup.md](code-cleanup.md) before deleting code, especially public API, parser logic, route handling, or benchmark-referenced behavior.
