package io.github.arthurhoch.kiss.server.http;

import java.util.Map;
import java.util.Objects;

/**
 * Convenience object passed to normal route handlers.
 *
 * <p>It exposes the parsed {@link Request}, dynamic path parameters, and common
 * response helpers. It is intentionally not a dependency injection container.</p>
 */
public final class Context {
    private final Request request;
    private final Map<String, String> pathParams;

    /**
     * Creates a context for a request and matched path parameters.
     */
    public Context(Request request, Map<String, String> pathParams) {
        this.request = Objects.requireNonNull(request, "request");
        this.pathParams = pathParams == null ? Map.of() : Map.copyOf(pathParams);
    }

    /**
     * Returns the parsed request.
     */
    public Request request() {
        return request;
    }

    /**
     * Returns a dynamic path parameter, or {@code null} when absent.
     */
    public String pathParam(String name) {
        return pathParams.get(name);
    }

    /**
     * Returns all dynamic path parameters.
     */
    public Map<String, String> pathParams() {
        return pathParams;
    }

    /**
     * Returns the request body decoded as UTF-8.
     */
    public String bodyAsString() {
        return request.bodyAsString();
    }

    /**
     * Creates a {@code 200 OK} text response.
     */
    public Response text(String body) {
        return Response.text(body);
    }

    /**
     * Creates a text response with the given status.
     */
    public Response text(HttpStatus status, String body) {
        return Response.text(status, body);
    }
}
