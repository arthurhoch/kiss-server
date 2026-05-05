# Buffer Management

## Goals

- Avoid uncontrolled memory growth.
- Avoid retaining oversized buffers.
- Keep implementation readable.
- Use bounded pools where useful.

## Pool

Use a bounded `byte[]` buffer pool for common read/write buffers. Return only buffers of the configured size. Drop unexpected oversized buffers.

## Request Body

Request body buffers must respect `maxBodyBytes`. Future streaming bodies must be explicit and tested.

## Parser Buffers

Parser buffers should be sized by configuration. They must not grow based on untrusted input without limit checks.

## Simplicity Rule

Do not introduce complex allocator abstractions unless measurements show a real problem.
