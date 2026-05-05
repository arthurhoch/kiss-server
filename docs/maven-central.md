# Maven Central

Publishing uses Sonatype Central Publisher Portal.

## Coordinates

```xml
<dependency>
  <groupId>io.github.arthurhoch</groupId>
  <artifactId>kiss-server</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Required Secrets

| Secret | Purpose |
|--------|---------|
| `MAVEN_CENTRAL_USERNAME` | Central Portal token username |
| `MAVEN_CENTRAL_PASSWORD` | Central Portal token password |
| `GPG_PRIVATE_KEY` | ASCII-armored GPG private key |
| `GPG_PASSPHRASE` | GPG key passphrase |

## Local Checks

```bash
mvn -B verify
mvn -B verify -P release
```

The release profile signs and publishes, so it needs GPG and Central configuration.

## Status

Release configuration is present. External account, namespace, key, and secret setup still must be completed before publishing.
