---
layout: default
---

# Android

Android compatibility is a design goal where reasonable, but it is not an official universal support claim yet.

## Current Claim

KissServer uses Java APIs intended to be Android-compatible by design, especially Java NIO networking APIs such as `ServerSocketChannel`, `SocketChannel`, `Selector`, and `ByteBuffer`.

Actual Android validation is still required before claiming support across devices, API levels, vendors, emulators, background modes, and network environments.

Actual Android instrumented validation is required before claiming support for all Android devices or environments.

## Friendly Choices

- Java 17 source compatibility for main code.
- Java standard library APIs.
- Java NIO socket APIs.
- Explicit `ExecutorService` configuration.
- No production dependencies.
- No reflection in core behavior.
- No classpath scanning.
- No annotation scanning.
- No generated classes.
- No dynamic proxies.
- No `ServiceLoader` requirement for core behavior.
- No JDK-internal APIs.
- No direct JDK 21 API calls from main source.

## Required Permission

Android applications that open network sockets need internet permission:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

Depending on binding and access pattern, the app may also need platform-specific network state handling and cleartext policy configuration.

## Lifecycle Issues

Running an HTTP server inside an Android app has platform constraints that are outside kiss-server core:

- foreground service requirements for long-running server behavior;
- app background execution restrictions;
- battery optimization and doze behavior;
- network changes between Wi-Fi, cellular, VPN, and hotspot modes;
- port binding conflicts;
- LAN accessibility and firewall behavior;
- loopback-only versus LAN binding;
- device sleep and app process death;
- user-visible notification requirements for foreground services.

The application owns these lifecycle decisions.

## Execution Profiles

- Java 17 NIO mode is the intended compatibility path by design.
- JDK 21 virtual-thread mode is optional JVM application code and benchmark code only.
- Do not require virtual threads for Android examples.
- Do not add Undertow, Vert.x, Netty, Jetty, Servlet, Spring, or Quarkus for Android compatibility.

## What Not To Claim Yet

Do not claim:

- universal Android device support;
- all API-level support;
- background service reliability;
- Play Store deployment readiness;
- validated emulator/device behavior.

Use wording such as "Android-compatible by design, pending real validation."

## Recommended Future android-sample

A future Android sample should include:

- a minimal Activity or foreground service;
- `INTERNET` permission;
- explicit bind host and port;
- start/stop lifecycle wiring;
- local curl or browser testing instructions;
- LAN access notes;
- emulator and physical-device validation matrix;
- battery/background behavior notes.
