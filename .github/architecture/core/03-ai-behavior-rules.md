# AI Behavior Rules

AI agents must:

- read `AGENTS.md` first;
- read `.github/ALL_MARKDOWN.md`;
- read this architecture index;
- read relevant module docs before implementation;
- run `mvn -B verify` before completion;
- update docs, tests, and changelog with public behavior changes.

AI agents must not:

- add production dependencies;
- introduce frameworks;
- implement HTTP/2 without an ADR;
- use reflection in the hot path;
- use parser regex, `String.split`, `Scanner`, or `BufferedReader.readLine`;
- claim planned behavior works before tests prove it.

Use `kiss-requests` and `kiss-json` as ecosystem and style references only.
