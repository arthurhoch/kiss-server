# GraalVM Native Image

KissServer must be Native Image friendly, but not GraalVM-dependent.

## Design

- No reflection for core behavior.
- No reflection in the hot path.
- No dynamic proxies.
- No annotation scanning.
- No runtime classpath scanning.
- No ServiceLoader requirement for core behavior.
- No JNI or Unsafe.
- No generated classes.
- No external runtime resources for normal usage.
- Explicit configuration through Java objects.
- Simple startup.

## Ownership

Native compilation is owned by the consuming application. Normal usage should not require:

- `reflection-config.json`;
- `resource-config.json`;
- `proxy-config.json`.

## Optional Helpers

The optional virtual-thread reflection helper is a JVM convenience. It is not the recommended Native Image path. Native Image users should pass an explicit executor.

## Claim Rule

Do not claim official Native Image support until validated with an example application or CI workflow.
