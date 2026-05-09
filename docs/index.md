---
layout: default
title: KissServer
---

<section class="hero">
  <div>
    <p class="eyebrow">KISS Java Libraries</p>
    <h1>KissServer</h1>
    <p class="lead">Tiny zero-dependency Java 17+ HTTP/1.1 server library for simple REST-style applications, explicit routing, bounded parsing, and predictable shutdown.</p>
    <div class="meta-row">
      <span class="tag">Latest stable: 0.1.0</span>
      <span class="tag">Java 17+</span>
      <span class="tag">Apache-2.0</span>
    </div>
    <div class="actions">
      <a class="button" href="getting-started.html">Getting Started</a>
      <a class="button secondary" href="api.html">API Reference</a>
      <a class="button secondary" href="https://github.com/arthurhoch/kiss-server">GitHub</a>
    </div>
  </div>
  <div class="panel">
    <p class="panel-title">Maven</p>
<pre><code>&lt;dependency&gt;
  &lt;groupId&gt;io.github.arthurhoch&lt;/groupId&gt;
  &lt;artifactId&gt;kiss-server&lt;/artifactId&gt;
  &lt;version&gt;0.1.0&lt;/version&gt;
&lt;/dependency&gt;</code></pre>
  </div>
</section>

<section class="section two-column">
  <div>
    <h2>Small Surface</h2>
    <p>KissServer keeps HTTP routes and runtime behavior explicit. It is intended for small services that need a direct Java API without servlet containers or web frameworks.</p>
  </div>
  <div class="panel">
    <p class="panel-title">Quick Example</p>
<pre><code>KissServer server = KissServer.create();
server.get("/health", ctx -&gt; ctx.text("OK"));
server.start(8080).await();</code></pre>
  </div>
</section>

<section class="section">
  <h2>KISS Principles</h2>
  <div class="feature-grid">
    <article class="feature">
      <h3>Bounded Input</h3>
      <p>Request line, header, body, connection, and keep-alive limits are explicit.</p>
    </article>
    <article class="feature">
      <h3>Direct Routing</h3>
      <p>Exact and dynamic routes use small public types with predictable request and response behavior.</p>
    </article>
    <article class="feature">
      <h3>Simple Runtime</h3>
      <p>The core artifact targets Java 17 and keeps optional Java 21 virtual-thread usage application-provided.</p>
    </article>
  </div>
</section>

<section class="section">
  <h2>Documentation</h2>
  <div class="doc-grid">
    <a href="getting-started.html">Getting Started<span>Install and create the first route.</span></a>
    <a href="api.html">API Reference<span>Public server, routing, HTTP, and runtime types.</span></a>
    <a href="skills/index.html">AI Skills<span>Versioned Markdown skill files for AI-assisted usage.</span></a>
    <a href="configuration.html">Configuration<span>ServerConfig options and limits.</span></a>
    <a href="examples.html">Examples<span>Copyable route examples.</span></a>
    <a href="http11.html">HTTP/1.1<span>Protocol scope and parser behavior.</span></a>
    <a href="routing.html">Routing<span>Exact routes, dynamic routes, and matching.</span></a>
    <a href="executor-model.html">Executor Model<span>User and owned executor behavior.</span></a>
    <a href="performance.html">Performance<span>Performance philosophy and measured paths.</span></a>
    <a href="benchmarking.html">Benchmarking<span>Manual benchmark plan and commands.</span></a>
    <a href="security.html">Security<span>Limits and safe defaults.</span></a>
    <a href="security-hardening.html">Security Hardening<span>Repository hardening and local quality commands.</span></a>
    <a href="code-cleanup.html">Safe Code Cleanup<span>Deletion policy and quality gates.</span></a>
    <a href="deployment.html">Deployment<span>Reverse proxy recommendation.</span></a>
    <a href="native-image.html">Native Image<span>GraalVM friendliness notes.</span></a>
    <a href="release.html">Release<span>Release process and Maven Central flow.</span></a>
    <a href="testing-report.html">Testing Report<span>Current verification state.</span></a>
  </div>
</section>

<section class="section">
  <h2>Related Projects</h2>
  <div class="related-grid">
    <a href="https://github.com/arthurhoch/kiss-json">kiss-json<span>Field-based JSON serialization and deserialization.</span></a>
    <a href="https://github.com/arthurhoch/kiss-requests">kiss-requests<span>Simple HTTP client built on Java HttpClient.</span></a>
    <a href="https://github.com/arthurhoch/kiss-server">kiss-server<span>Small HTTP/1.1 server for simple REST-style applications.</span></a>
    <a href="https://github.com/arthurhoch/kiss-config">kiss-config<span>Configuration from properties, .env, system properties, and environment variables.</span></a>
    <a href="https://github.com/arthurhoch/kiss-binary">kiss-binary<span>Explicit binary IO for primitive binary formats.</span></a>
  </div>
</section>
