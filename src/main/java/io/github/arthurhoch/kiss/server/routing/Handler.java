package io.github.arthurhoch.kiss.server.routing;

import io.github.arthurhoch.kiss.server.http.Context;
import io.github.arthurhoch.kiss.server.http.Response;

/**
 * Normal route handler.
 *
 * <p>Handlers receive a {@link Context} and return a {@link Response}. In the
 * NIO engine they run on the configured executor by default.</p>
 */
@FunctionalInterface
public interface Handler {
    /**
     * Handles one request.
     *
     * @param context request context
     * @return response to send
     * @throws Exception when the handler fails; the server maps failures to a safe error response
     */
    Response handle(Context context) throws Exception;
}
