# Android Compatibility

KissServer should avoid APIs unavailable on Android where reasonable, but official Android support is not claimed yet.

## Baseline Goal

- Java 17 source.
- Java NIO networking APIs: `ServerSocketChannel`, `SocketChannel`, `Selector`, and `ByteBuffer`.
- Explicit `ExecutorService` configuration.
- No production dependencies.
- No dynamic proxies, classpath scanning, annotation scanning, or generated classes.
- No direct JDK 21 APIs in main source.
- No `ServiceLoader` requirement for core behavior.

## Permission

Android applications that open network sockets need:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## Lifecycle Concerns

Android server apps must account for foreground service rules, background restrictions, battery optimization, network changes, port binding, LAN accessibility, cleartext policy, and app process lifetime.

These concerns belong to the Android application or sample, not the kiss-server core.

## Optional JVM Profile

JDK 21 virtual threads are application-provided executors. They are allowed in benchmark apps and clearly marked optional examples, but must not become an Android requirement or a main-artifact compile requirement.

## Claim Rule

Use "Android-compatible by design, pending real validation." Do not claim universal Android device support until instrumented Android tests or a validated sample exists.

## Future Sample

A future `android-sample` should include permission, foreground-service lifecycle, start/stop controls, emulator and device validation notes, and LAN access instructions.
