package io.github.arthurhoch.kiss.server.http;

import java.util.Locale;

public enum HttpMethod {
    GET,
    POST,
    PUT,
    DELETE,
    PATCH,
    HEAD,
    OPTIONS;

    public static HttpMethod from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("HTTP method must not be blank");
        }
        return HttpMethod.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
