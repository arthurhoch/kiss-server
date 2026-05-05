# Roadmap

## Phase 1: Foundation

Complete:

- Maven setup;
- docs and architecture;
- workflows and Dependabot;
- compile-safe skeleton;
- basic configuration tests.

## Phase 2: HTTP/1.1 Core Engine

Complete for initial core:

- socket accept loop;
- connection lifecycle;
- byte parser;
- response writer;
- keep-alive;
- timeouts;
- routing integration;
- real socket integration tests.

## Phase 3: Hardening

In progress:

- malformed input matrix;
- limit tests;
- shutdown tests;
- benchmark setup;
- security review.

## Phase 4: Release

Planned:

- release docs final review;
- first local consumer test;
- Maven Central publish.
