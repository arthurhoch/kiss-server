package io.github.arthurhoch.kiss.server.routing;

import io.github.arthurhoch.kiss.server.http.HttpMethod;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class Route {
    private final HttpMethod method;
    private final String path;
    private final Handler handler;
    private final DirectHandler directHandler;
    private final byte[] directResponse;

    public Route(
            HttpMethod method,
            String path,
            Handler handler,
            DirectHandler directHandler
    ) {
        this(method, path, handler, directHandler, null);
    }

    private Route(
            HttpMethod method,
            String path,
            Handler handler,
            DirectHandler directHandler,
            byte[] directResponse
    ) {
        this.method = Objects.requireNonNull(method, "method");
        requirePath(path);
        this.path = path;
        if ((handler == null) == (directHandler == null)) {
            throw new IllegalArgumentException("route must have exactly one handler");
        }
        this.handler = handler;
        this.directHandler = directHandler;
        this.directResponse = directResponse == null ? null : directResponse.clone();
    }

    public static Route normal(HttpMethod method, String path, Handler handler) {
        return new Route(method, path, Objects.requireNonNull(handler, "handler"), null, null);
    }

    public static Route direct(HttpMethod method, String path, DirectHandler handler) {
        return new Route(method, path, null, Objects.requireNonNull(handler, "handler"), null);
    }

    static Route directResponse(HttpMethod method, String path, byte[] response) {
        byte[] safeResponse = Objects.requireNonNull(response, "response").clone();
        return new Route(method, path, null, () -> safeResponse, safeResponse);
    }

    public HttpMethod method() {
        return method;
    }

    public String path() {
        return path;
    }

    public Handler handler() {
        return handler;
    }

    public DirectHandler directHandler() {
        return directHandler;
    }

    public byte[] directResponse() {
        return directResponse == null ? null : directResponse.clone();
    }

    public boolean hasPrebuiltDirectResponse() {
        return directResponse != null;
    }

    public byte[] directResponseUnsafe() {
        return directResponse;
    }

    public boolean fast() {
        return directHandler != null;
    }

    public boolean exact() {
        return path.indexOf('{') < 0;
    }

    public Optional<Map<String, String>> match(String requestPath) {
        Objects.requireNonNull(requestPath, "requestPath");
        Map<String, String> params = matchParamsOrNull(requestPath);
        return params == null ? Optional.empty() : Optional.of(params);
    }

    private static void requirePath(String path) {
        Objects.requireNonNull(path, "path");
        if (path.isBlank() || path.charAt(0) != '/') {
            throw new IllegalArgumentException("route path must start with /");
        }
    }

    Map<String, String> matchParamsOrNull(String requestPath) {
        Objects.requireNonNull(requestPath, "requestPath");
        if (exact()) {
            return path.equals(requestPath) ? Map.of() : null;
        }
        Map<String, String> params = null;
        int patternStart = 1;
        int requestStart = 1;
        while (true) {
            int patternEnd = nextSlash(path, patternStart);
            int requestEnd = nextSlash(requestPath, requestStart);
            int patternLength = patternEnd - patternStart;
            int requestLength = requestEnd - requestStart;
            if (isParameter(path, patternStart, patternEnd)) {
                if (requestLength == 0) {
                    return null;
                }
                if (params == null) {
                    params = new LinkedHashMap<>();
                }
                params.put(path.substring(patternStart + 1, patternEnd - 1),
                        requestPath.substring(requestStart, requestEnd));
            } else if (patternLength != requestLength
                    || !path.regionMatches(patternStart, requestPath, requestStart, patternLength)) {
                return null;
            }
            boolean patternDone = patternEnd == path.length();
            boolean requestDone = requestEnd == requestPath.length();
            if (patternDone && requestDone) {
                return params == null ? Map.of() : params;
            }
            if (patternDone || requestDone) {
                return null;
            }
            patternStart = patternEnd + 1;
            requestStart = requestEnd + 1;
        }
    }

    boolean matchesRequestPath(String requestPath) {
        Objects.requireNonNull(requestPath, "requestPath");
        if (exact()) {
            return path.equals(requestPath);
        }
        int patternStart = 1;
        int requestStart = 1;
        while (true) {
            int patternEnd = nextSlash(path, patternStart);
            int requestEnd = nextSlash(requestPath, requestStart);
            int patternLength = patternEnd - patternStart;
            int requestLength = requestEnd - requestStart;
            if (isParameter(path, patternStart, patternEnd)) {
                if (requestLength == 0) {
                    return false;
                }
            } else if (patternLength != requestLength
                    || !path.regionMatches(patternStart, requestPath, requestStart, patternLength)) {
                return false;
            }
            boolean patternDone = patternEnd == path.length();
            boolean requestDone = requestEnd == requestPath.length();
            if (patternDone && requestDone) {
                return true;
            }
            if (patternDone || requestDone) {
                return false;
            }
            patternStart = patternEnd + 1;
            requestStart = requestEnd + 1;
        }
    }

    private static boolean isParameter(String value, int start, int end) {
        return end - start > 2 && value.charAt(start) == '{' && value.charAt(end - 1) == '}';
    }

    private static int nextSlash(String value, int start) {
        for (int i = start; i < value.length(); i++) {
            if (value.charAt(i) == '/') {
                return i;
            }
        }
        return value.length();
    }
}
