# System Purpose

KissServer is a small zero-dependency Java HTTP/1.1 server for:

- simple APIs;
- local tools;
- MVPs;
- internal services;
- embedded use cases;
- AI-generated applications;
- Android-compatible scenarios where reasonable.

KissServer complements:

- `kiss-requests` - simple HTTP client;
- `kiss-json` - simple JSON library;
- `kiss-server` - simple HTTP/1.1 server.

It must not depend on either sibling library.

## Why It Exists

Many Java server options are frameworks or large networking stacks. KissServer exists for cases where a small, explicit, standard-library socket server is easier to inspect, embed, test, and reason about.

## Non-Goal

KissServer is not a general application framework. It must not grow into a dependency injection container, servlet container, plugin runtime, or full edge server.
