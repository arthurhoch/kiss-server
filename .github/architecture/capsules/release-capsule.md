# Release Capsule

Use this for release and publishing changes.

## Rules

- Keep `mvn -B verify` secret-free.
- Publish only through the release profile.
- Keep Central Portal server id `central`.
- Required secrets are documented in release workflow and docs.
- Update `CHANGELOG.md` before release.
- Verify sources and Javadocs are attached.
