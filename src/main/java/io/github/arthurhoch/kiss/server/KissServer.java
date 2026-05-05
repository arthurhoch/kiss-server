package io.github.arthurhoch.kiss.server;

import io.github.arthurhoch.kiss.server.http.HttpMethod;
import io.github.arthurhoch.kiss.server.protocol.http11.Http11ServerEngine;
import io.github.arthurhoch.kiss.server.routing.DirectHandler;
import io.github.arthurhoch.kiss.server.routing.Handler;
import io.github.arthurhoch.kiss.server.routing.Router;

import java.util.Objects;

/**
 * Main facade for registering routes and starting a kiss-server HTTP/1.1 server.
 *
 * <p>Normal routes run through {@link Handler}. Exact fixed {@code GET}
 * responses can use {@link #fastGet(String, byte[])} with
 * {@code FastResponses} for lower allocation.</p>
 */
public final class KissServer {
    private final ServerConfig config;
    private final Router router;

    private KissServer(ServerConfig config) {
        this.config = Objects.requireNonNull(config, "config");
        this.router = new Router();
    }

    /**
     * Creates a server with default configuration.
     *
     * @return a new server
     */
    public static KissServer create() {
        return new KissServer(ServerConfig.defaults());
    }

    /**
     * Creates a server with the provided immutable configuration.
     *
     * @param config server configuration
     * @return a new server
     */
    public static KissServer create(ServerConfig config) {
        return new KissServer(config);
    }

    /**
     * Returns the configuration used by this server.
     *
     * @return server configuration
     */
    public ServerConfig config() {
        return config;
    }

    /**
     * Returns the router backing this server.
     *
     * @return router
     */
    public Router router() {
        return router;
    }

    /**
     * Registers a normal {@code GET} route.
     */
    public KissServer get(String path, Handler handler) {
        return route(HttpMethod.GET, path, handler);
    }

    /**
     * Registers a normal {@code POST} route.
     */
    public KissServer post(String path, Handler handler) {
        return route(HttpMethod.POST, path, handler);
    }

    /**
     * Registers a normal {@code PUT} route.
     */
    public KissServer put(String path, Handler handler) {
        return route(HttpMethod.PUT, path, handler);
    }

    /**
     * Registers a normal {@code DELETE} route.
     */
    public KissServer delete(String path, Handler handler) {
        return route(HttpMethod.DELETE, path, handler);
    }

    /**
     * Registers a normal {@code PATCH} route.
     */
    public KissServer patch(String path, Handler handler) {
        return route(HttpMethod.PATCH, path, handler);
    }

    /**
     * Registers a normal {@code HEAD} route.
     */
    public KissServer head(String path, Handler handler) {
        return route(HttpMethod.HEAD, path, handler);
    }

    /**
     * Registers a normal {@code OPTIONS} route.
     */
    public KissServer options(String path, Handler handler) {
        return route(HttpMethod.OPTIONS, path, handler);
    }

    /**
     * Registers a normal route for the given HTTP method and path.
     *
     * @return this server for chaining
     */
    public KissServer route(HttpMethod method, String path, Handler handler) {
        router.add(method, path, handler);
        return this;
    }

    /**
     * Registers a direct exact route that returns a complete HTTP response as bytes.
     *
     * <p>Most application code should prefer normal routes or
     * {@link #fastGet(String, byte[])} for fixed {@code GET} responses.</p>
     *
     * @return this server for chaining
     */
    public KissServer direct(HttpMethod method, String path, DirectHandler handler) {
        router.addDirect(method, path, handler);
        return this;
    }

    /**
     * Registers an exact fixed {@code GET} response.
     *
     * <p>Use for static endpoints such as {@code /health}, {@code /ready}, and
     * {@code /version}. Do not use for dynamic business logic.</p>
     *
     * @param path exact route path
     * @param response complete prebuilt HTTP/1.1 response bytes
     * @return this server for chaining
     */
    public KissServer fastGet(String path, byte[] response) {
        router.addDirectResponse(HttpMethod.GET, path, Objects.requireNonNull(response, "response"));
        return this;
    }

    /**
     * Starts the server using the configured host and port.
     *
     * @return handle for awaiting or stopping the server
     */
    public ServerHandle start() {
        return new Http11ServerEngine(config, router).start();
    }

    /**
     * Starts the server on the configured host and the provided port.
     *
     * @param port port to bind, or {@code 0} for an operating-system assigned port
     * @return handle for awaiting or stopping the server
     */
    public ServerHandle start(int port) {
        return new Http11ServerEngine(config.withPort(port), router).start();
    }
}
