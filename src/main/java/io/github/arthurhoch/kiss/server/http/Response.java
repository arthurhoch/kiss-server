package io.github.arthurhoch.kiss.server.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * HTTP response returned by normal route handlers.
 *
 * <p>Response header names and values are validated to prevent header
 * injection. The HTTP/1.1 writer computes {@code Content-Length} from the
 * stored body.</p>
 */
public final class Response {
    private static final byte[] EMPTY_BODY = new byte[0];

    private final HttpStatus status;
    private final Map<String, String> headers;
    private final byte[] body;

    private Response(HttpStatus status, Map<String, String> headers, byte[] body) {
        this.status = Objects.requireNonNull(status, "status");
        this.headers = validatedHeaders(headers);
        this.body = body == null ? EMPTY_BODY : body;
    }

    /**
     * Creates an empty response with the given status.
     */
    public static Response status(HttpStatus status) {
        return new Response(status, Map.of(), EMPTY_BODY);
    }

    /**
     * Creates a {@code 200 OK} UTF-8 text response.
     */
    public static Response text(String body) {
        return text(HttpStatus.OK, body);
    }

    /**
     * Creates a UTF-8 text response with the given status.
     */
    public static Response text(HttpStatus status, String body) {
        return body(status, ContentType.TEXT_PLAIN, body == null ? "" : body, StandardCharsets.UTF_8);
    }

    /**
     * Creates a string body response with an explicit content type and charset.
     */
    public static Response body(HttpStatus status, String contentType, String body, Charset charset) {
        Objects.requireNonNull(charset, "charset");
        HttpHeaders.requireSafeValue(contentType);
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, contentType);
        return new Response(status, headers, (body == null ? "" : body).getBytes(charset));
    }

    /**
     * Returns a copy of this response with an additional header.
     */
    public Response header(String name, String value) {
        HttpHeaders.requireValidName(name);
        HttpHeaders.requireSafeValue(value);
        Map<String, String> next = new LinkedHashMap<>(headers);
        next.put(name, value);
        return new Response(status, next, body);
    }

    /**
     * Returns the response status.
     */
    public HttpStatus status() {
        return status;
    }

    /**
     * Returns response headers.
     */
    public Map<String, String> headers() {
        return headers;
    }

    /**
     * Returns a defensive copy of the response body.
     */
    public byte[] body() {
        return body.clone();
    }

    /**
     * Returns the response body length in bytes.
     */
    public int bodyLength() {
        return body.length;
    }

    /**
     * Writes the response body to an output stream.
     */
    public void writeBodyTo(OutputStream output) throws IOException {
        Objects.requireNonNull(output, "output");
        output.write(body);
    }

    private static Map<String, String> validatedHeaders(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return Map.of();
        }
        Map<String, String> safe = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            HttpHeaders.requireValidName(entry.getKey());
            HttpHeaders.requireSafeValue(entry.getValue());
            safe.put(entry.getKey(), entry.getValue());
        }
        return Collections.unmodifiableMap(safe);
    }
}
