# Release

KissServer follows semantic versioning.

## Tag-Based Release

```bash
git tag v0.1.0
git push origin v0.1.0
```

The Maven Central workflow also supports manual dispatch.

## Pre-Release Checklist

1. Run `mvn -B verify`.
2. Update `CHANGELOG.md`.
3. Update docs for public behavior changes.
4. Set `pom.xml` to the release version.
5. Commit the release-ready state.
6. Create and push a `v*` tag.

## Post-Release

1. Verify the artifact on Central Portal and Maven Central.
2. Bump `pom.xml` to the next `-SNAPSHOT`.
3. Commit the snapshot version bump.
