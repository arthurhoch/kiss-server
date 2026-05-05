package io.github.arthurhoch.kiss.server.protocol.http11;

import io.github.arthurhoch.kiss.server.http.HttpMethod;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public final class ParsedRequest {
    private static final byte[] EMPTY_BODY = new byte[0];

    private final HttpMethod method;
    private final String target;
    private final String version;
    private final Map<String, String> headers;
    private final byte[] body;

    public ParsedRequest(
            HttpMethod method,
            String target,
            String version,
            Map<String, String> headers,
            byte[] body
    ) {
        this(method, target, version, headers, body, true);
    }

    private ParsedRequest(
            HttpMethod method,
            String target,
            String version,
            Map<String, String> headers,
            byte[] body,
            boolean copyValues
    ) {
        this.method = Objects.requireNonNull(method, "method");
        this.target = Objects.requireNonNull(target, "target");
        this.version = Objects.requireNonNull(version, "version");
        if (headers == null || headers.isEmpty()) {
            this.headers = Map.of();
        } else {
            this.headers = copyValues ? Map.copyOf(headers) : headers;
        }
        if (body == null || body.length == 0) {
            this.body = EMPTY_BODY;
        } else {
            this.body = copyValues ? body.clone() : body;
        }
    }

    static ParsedRequest trusted(
            HttpMethod method,
            String target,
            String version,
            Map<String, String> headers,
            byte[] body
    ) {
        Map<String, String> safeHeaders = headers == null ? Map.of() : Collections.unmodifiableMap(headers);
        return new ParsedRequest(method, target, version, safeHeaders, body == null ? EMPTY_BODY : body, false);
    }

    static byte[] emptyBody() {
        return EMPTY_BODY;
    }

    public HttpMethod method() {
        return method;
    }

    public String target() {
        return target;
    }

    public String version() {
        return version;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public byte[] body() {
        return body.clone();
    }

    byte[] bodyUnsafe() {
        return body;
    }

    @Override
    public boolean equals(Object value) {
        if (this == value) {
            return true;
        }
        if (!(value instanceof ParsedRequest other)) {
            return false;
        }
        return method == other.method
                && target.equals(other.target)
                && version.equals(other.version)
                && headers.equals(other.headers)
                && Arrays.equals(body, other.body);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(method, target, version, headers);
        result = 31 * result + Arrays.hashCode(body);
        return result;
    }
}
