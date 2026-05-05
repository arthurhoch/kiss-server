package io.github.arthurhoch.kiss.server.routing;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public final class RouteMatch {
    private final Route route;
    private final Map<String, String> pathParams;

    public RouteMatch(Route route, Map<String, String> pathParams) {
        this(route, pathParams, true);
    }

    private RouteMatch(Route route, Map<String, String> pathParams, boolean copyPathParams) {
        this.route = Objects.requireNonNull(route, "route");
        if (pathParams == null || pathParams.isEmpty()) {
            this.pathParams = Map.of();
        } else {
            this.pathParams = copyPathParams ? Map.copyOf(pathParams) : pathParams;
        }
    }

    static RouteMatch trusted(Route route, Map<String, String> pathParams) {
        Map<String, String> safeParams = pathParams == null || pathParams.isEmpty()
                ? Map.of()
                : Collections.unmodifiableMap(pathParams);
        return new RouteMatch(route, safeParams, false);
    }

    public Route route() {
        return route;
    }

    public Map<String, String> pathParams() {
        return pathParams;
    }
}
