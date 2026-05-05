# Release and Maven Central

## Coordinates

```xml
<groupId>io.github.arthurhoch</groupId>
<artifactId>kiss-server</artifactId>
```

## Versions

- Development uses `-SNAPSHOT`.
- Releases use semantic versions, for example `0.1.0`.
- Tags use `v*`, for example `v0.1.0`.

## Maven Central Requirements

`pom.xml` must include:

- name;
- description;
- URL;
- license;
- developer;
- SCM;
- sources JAR;
- Javadocs JAR;
- GPG signing;
- Central Portal publishing plugin.

## Secrets

Required GitHub secrets:

- `MAVEN_CENTRAL_USERNAME`;
- `MAVEN_CENTRAL_PASSWORD`;
- `GPG_PRIVATE_KEY`;
- `GPG_PASSPHRASE`.

## Workflow

The release workflow runs tests, imports GPG, and executes:

```bash
mvn -B deploy -P release
```

CI must never require publishing secrets.
