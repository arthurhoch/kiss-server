# Code Generation Rules

Generated or AI-written code must be:

- Java 17 compatible;
- zero production dependency;
- readable without tool-specific context;
- covered by tests when behavior changes;
- documented when it affects public API or architecture.

## Forbidden

- Lombok.
- Annotation processing for core behavior.
- Runtime class generation.
- Dynamic proxies for core behavior.
- ServiceLoader for core behavior.
- Classpath scanning.
- JNI or Unsafe.

## Parser-Specific Rule

HTTP parser code must be hand-written and byte-oriented. It must not use regex, `String.split`, `Scanner`, or `BufferedReader.readLine`.
