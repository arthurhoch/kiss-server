---
layout: default
---

# Prompt: Generate A kiss-server App

```text
Create a Java 17 Maven application using kiss-server.

Requirements:
- Use io.github.arthurhoch:kiss-server.
- Do not add Spring, Quarkus, Netty, Jetty, Undertow, Vert.x, Servlet, or any other HTTP framework.
- Do not add a JSON dependency unless I explicitly request one.
- Use fastGet with FastResponses for fixed /health and /version endpoints.
- Use normal get/post routes for business endpoints, dynamic paths, query-dependent responses, and request bodies.
- Configure an explicit ExecutorService in ServerConfig.
- Keep examples and source compatible with Java 17.
- If showing JDK 21 virtual threads, mark it optional application code only.
- Add focused tests for the generated routes.
- Add curl commands for manual testing.
- Add wrk commands with warmup and measured runs.
- Document which routes are fast path and which are normal path.
```
