package io.github.arthurhoch.kiss.server.routing;

import io.github.arthurhoch.kiss.server.http.HttpMethod;

import java.util.Optional;

/**
 * Placeholder for the future dynamic route trie.
 */
public final class RouteTrie {
    private final Router router = new Router();

    public void add(HttpMethod method, String path, Handler handler) {
        router.add(method, path, handler);
    }

    public Optional<RouteMatch> match(HttpMethod method, String path) {
        return router.match(method, path);
    }
}
