package io.github.arthurhoch.kiss.server.routing;

import io.github.arthurhoch.kiss.server.http.HttpMethod;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class Router {
    private final Map<HttpMethod, List<Route>> routes = new EnumMap<>(HttpMethod.class);
    private final Map<HttpMethod, Map<String, Route>> fastExactRoutes = new EnumMap<>(HttpMethod.class);
    private final Map<HttpMethod, Map<String, Route>> exactRoutes = new EnumMap<>(HttpMethod.class);
    private final Map<HttpMethod, List<Route>> dynamicRoutes = new EnumMap<>(HttpMethod.class);

    public void add(HttpMethod method, String path, Handler handler) {
        add(Route.normal(method, path, handler));
    }

    public void addDirect(HttpMethod method, String path, DirectHandler handler) {
        Route route = Route.direct(method, path, handler);
        if (!route.exact()) {
            throw new IllegalArgumentException("direct routes must be exact");
        }
        add(route);
    }

    public void addDirectResponse(HttpMethod method, String path, byte[] response) {
        Route route = Route.directResponse(method, path, response);
        if (!route.exact()) {
            throw new IllegalArgumentException("direct routes must be exact");
        }
        add(route);
    }

    public Optional<RouteMatch> match(HttpMethod method, String path) {
        return Optional.ofNullable(matchOrNull(method, path));
    }

    public RouteMatch matchOrNull(HttpMethod method, String path) {
        Objects.requireNonNull(method, "method");
        Objects.requireNonNull(path, "path");
        String requestPath = pathOnly(path);

        Route fast = fastExactRoutes.getOrDefault(method, Map.of()).get(requestPath);
        if (fast != null) {
            return RouteMatch.trusted(fast, Map.of());
        }
        Route exact = exactRoutes.getOrDefault(method, Map.of()).get(requestPath);
        if (exact != null) {
            return RouteMatch.trusted(exact, Map.of());
        }
        for (Route route : dynamicRoutes.getOrDefault(method, List.of())) {
            Map<String, String> params = route.matchParamsOrNull(requestPath);
            if (params != null) {
                return RouteMatch.trusted(route, params);
            }
        }
        return null;
    }

    public List<Route> routes(HttpMethod method) {
        return List.copyOf(routes.getOrDefault(method, List.of()));
    }

    public boolean matchesPath(String path) {
        return !allowedMethods(path).isEmpty();
    }

    public EnumSet<HttpMethod> allowedMethods(String path) {
        Objects.requireNonNull(path, "path");
        String requestPath = pathOnly(path);
        EnumSet<HttpMethod> allowed = EnumSet.noneOf(HttpMethod.class);
        for (HttpMethod method : HttpMethod.values()) {
            if (matchesMethod(method, requestPath)) {
                allowed.add(method);
            }
        }
        return allowed;
    }

    private void add(Route route) {
        routes.computeIfAbsent(route.method(), ignored -> new ArrayList<>()).add(route);
        if (route.exact()) {
            Map<HttpMethod, Map<String, Route>> exactTarget = route.fast() ? fastExactRoutes : exactRoutes;
            exactTarget.computeIfAbsent(route.method(), ignored -> new HashMap<>()).put(route.path(), route);
        } else {
            dynamicRoutes.computeIfAbsent(route.method(), ignored -> new ArrayList<>()).add(route);
        }
    }

    private boolean matchesMethod(HttpMethod method, String path) {
        if (fastExactRoutes.getOrDefault(method, Map.of()).containsKey(path)
                || exactRoutes.getOrDefault(method, Map.of()).containsKey(path)) {
            return true;
        }
        for (Route route : dynamicRoutes.getOrDefault(method, List.of())) {
            if (route.matchesRequestPath(path)) {
                return true;
            }
        }
        return false;
    }

    private static String pathOnly(String path) {
        int queryStart = path.indexOf('?');
        return queryStart < 0 ? path : path.substring(0, queryStart);
    }
}
