package io.github.arthurhoch.kiss.server.http;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * Parsed HTTP request exposed to normal route handlers.
 *
 * <p>Request bodies and header maps are defensively copied. Use
 * {@link #bodyAsString()} only when a text body is needed.</p>
 */
public final class Request {
    private static final byte[] EMPTY_BODY = new byte[0];

    private final HttpMethod method;
    private final String path;
    private final String queryString;
    private final Map<String, String> headers;
    private final byte[] body;
    private String cachedUtf8Body;

    /**
     * Creates a request value.
     */
    public Request(
            HttpMethod method,
            String path,
            String queryString,
            Map<String, String> headers,
            byte[] body
    ) {
        this.method = Objects.requireNonNull(method, "method");
        this.path = Objects.requireNonNull(path, "path");
        this.queryString = queryString == null ? "" : queryString;
        this.headers = headers == null ? Map.of() : Map.copyOf(headers);
        this.body = body == null ? EMPTY_BODY : body.clone();
    }

    /**
     * Returns the HTTP method.
     */
    public HttpMethod method() {
        return method;
    }

    /**
     * Returns the request path without the query string.
     */
    public String path() {
        return path;
    }

    /**
     * Returns the raw query string without the leading {@code ?}.
     */
    public String queryString() {
        return queryString;
    }

    /**
     * Returns request headers as an immutable map.
     */
    public Map<String, String> headers() {
        return headers;
    }

    /**
     * Returns a defensive copy of the request body bytes.
     */
    public byte[] body() {
        return body.clone();
    }

    /**
     * Returns the body decoded as UTF-8 and caches the decoded string.
     */
    public String bodyAsString() {
        String value = cachedUtf8Body;
        if (value == null) {
            value = new String(body, StandardCharsets.UTF_8);
            cachedUtf8Body = value;
        }
        return value;
    }

    /**
     * Returns the body decoded with the provided charset.
     */
    public String bodyAsString(Charset charset) {
        Objects.requireNonNull(charset, "charset");
        if (StandardCharsets.UTF_8.equals(charset)) {
            return bodyAsString();
        }
        return new String(body, charset);
    }

    /**
     * Returns a header value by case-insensitive name, or {@code null} when absent.
     */
    public String header(String name) {
        return HttpHeaders.get(headers, name);
    }

    @Override
    public boolean equals(Object value) {
        if (this == value) {
            return true;
        }
        if (!(value instanceof Request other)) {
            return false;
        }
        return method == other.method
                && path.equals(other.path)
                && queryString.equals(other.queryString)
                && headers.equals(other.headers)
                && Arrays.equals(body, other.body);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(method, path, queryString, headers);
        result = 31 * result + Arrays.hashCode(body);
        return result;
    }
}
