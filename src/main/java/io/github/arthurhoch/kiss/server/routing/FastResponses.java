package io.github.arthurhoch.kiss.server.routing;

import io.github.arthurhoch.kiss.server.http.HttpHeaders;
import io.github.arthurhoch.kiss.server.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Factory for complete prebuilt HTTP/1.1 responses used by fast exact routes.
 *
 * <p>Use with {@code KissServer.fastGet(...)} for fixed static responses such
 * as health, readiness, version, or other request-independent endpoints.</p>
 */
public final class FastResponses {
    private static final byte[] CRLF = "\r\n".getBytes(StandardCharsets.US_ASCII);
    private static final String FAST_TEXT_PLAIN = "text/plain";
    private static final String FAST_JSON = "application/json";

    private FastResponses() {
    }

    /**
     * Creates a {@code 200 OK} plain text response.
     */
    public static byte[] text(String body) {
        return text(HttpStatus.OK, body);
    }

    /**
     * Creates a plain text response with the given status.
     */
    public static byte[] text(HttpStatus status, String body) {
        Objects.requireNonNull(status, "status");
        byte[] payload = (body == null ? "" : body).getBytes(StandardCharsets.UTF_8);
        return response(status, FAST_TEXT_PLAIN, payload);
    }

    /**
     * Creates a {@code 200 OK} JSON response.
     */
    public static byte[] json(String body) {
        return json(HttpStatus.OK, body);
    }

    /**
     * Creates a JSON response with the given status.
     */
    public static byte[] json(HttpStatus status, String body) {
        Objects.requireNonNull(status, "status");
        byte[] payload = (body == null ? "" : body).getBytes(StandardCharsets.UTF_8);
        return response(status, FAST_JSON, payload);
    }

    /**
     * Creates a {@code 204 No Content} response.
     */
    public static byte[] noContent() {
        return empty(HttpStatus.NO_CONTENT);
    }

    /**
     * Creates an empty response with the given status.
     */
    public static byte[] empty(HttpStatus status) {
        Objects.requireNonNull(status, "status");
        String head = "HTTP/1.1 " + status.code() + " " + status.reason() + "\r\n"
                + HttpHeaders.CONTENT_LENGTH + ": 0\r\n"
                + "\r\n";
        return head.getBytes(StandardCharsets.US_ASCII);
    }

    /**
     * Returns a copy of the response with a per-request {@code Connection} header.
     */
    public static byte[] withConnection(byte[] response, boolean includeBody, boolean keepAlive) {
        Objects.requireNonNull(response, "response");
        int headerEnd = headerEnd(response);
        if (headerEnd < 0) {
            return response.clone();
        }
        int bodyStart = headerEnd + 4;
        int bodyLength = includeBody ? response.length - bodyStart : 0;
        byte[] connection = (HttpHeaders.CONNECTION + ": " + (keepAlive ? "keep-alive" : "close") + "\r\n")
                .getBytes(StandardCharsets.US_ASCII);
        byte[] result = new byte[headerEnd + 2 + connection.length + 2 + bodyLength];
        System.arraycopy(response, 0, result, 0, headerEnd + 2);
        System.arraycopy(connection, 0, result, headerEnd + 2, connection.length);
        int endOffset = headerEnd + 2 + connection.length;
        result[endOffset] = '\r';
        result[endOffset + 1] = '\n';
        if (bodyLength > 0) {
            System.arraycopy(response, bodyStart, result, endOffset + 2, bodyLength);
        }
        return result;
    }

    /**
     * Returns a CRLF byte sequence.
     */
    public static byte[] crlf() {
        return CRLF.clone();
    }

    private static byte[] response(HttpStatus status, String contentType, byte[] payload) {
        String head = "HTTP/1.1 " + status.code() + " " + status.reason() + "\r\n"
                + HttpHeaders.CONTENT_TYPE + ": " + contentType + "\r\n"
                + HttpHeaders.CONTENT_LENGTH + ": " + payload.length + "\r\n"
                + "\r\n";
        byte[] header = head.getBytes(StandardCharsets.US_ASCII);
        byte[] result = new byte[header.length + payload.length];
        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(payload, 0, result, header.length, payload.length);
        return result;
    }

    private static int headerEnd(byte[] response) {
        for (int i = 0; i <= response.length - 4; i++) {
            if (response[i] == '\r'
                    && response[i + 1] == '\n'
                    && response[i + 2] == '\r'
                    && response[i + 3] == '\n') {
                return i;
            }
        }
        return -1;
    }
}
