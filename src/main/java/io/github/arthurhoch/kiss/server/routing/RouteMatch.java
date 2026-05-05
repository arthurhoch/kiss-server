package io.github.arthurhoch.kiss.server.routing;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public final class RouteMatch {
    private final Route route;
    private final Map<String, String> pathParams;

    public RouteMatch(Route route, Map<String, String> pathParams) {
        this(route, pathParams == null ? Map.of() : Map.copyOf(pathParams), true);
    }

    private RouteMatch(Route route, Map<String, String> pathParams, boolean trusted) {
        this.route = Objects.requireNonNull(route, "route");
        this.pathParams = Objects.requireNonNull(pathParams, "pathParams");
    }

    static RouteMatch trusted(Route route, Map<String, String> pathParams) {
        Map<String, String> safeParams = pathParams == null || pathParams.isEmpty()
                ? Map.of()
                : Collections.unmodifiableMap(pathParams);
        return new RouteMatch(route, safeParams, true);
    }

    public Route route() {
        return route;
    }

    public Map<String, String> pathParams() {
        return pathParams;
    }
}
