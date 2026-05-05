package io.github.arthurhoch.kiss.server.http;

import java.util.Map;

public final class HttpHeaders {
    public static final String CONNECTION = "Connection";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String HOST = "Host";
    public static final String TRANSFER_ENCODING = "Transfer-Encoding";

    private HttpHeaders() {
    }

    public static void requireValidName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("header name must not be blank");
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            boolean valid = c == '!' || c == '#' || c == '$' || c == '%' || c == '&'
                    || c == '\'' || c == '*' || c == '+' || c == '-' || c == '.'
                    || c == '^' || c == '_' || c == '`' || c == '|' || c == '~'
                    || (c >= '0' && c <= '9')
                    || (c >= 'A' && c <= 'Z')
                    || (c >= 'a' && c <= 'z');
            if (!valid) {
                throw new IllegalArgumentException("invalid header name: " + name);
            }
        }
    }

    public static void requireSafeValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("header value must not be null");
        }
        if (value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0) {
            throw new IllegalArgumentException("header value must not contain CR or LF");
        }
    }

    public static boolean equalsName(String left, String right) {
        return left != null && right != null && left.equalsIgnoreCase(right);
    }

    public static boolean contains(Map<String, String> headers, String name) {
        return get(headers, name) != null;
    }

    public static String get(Map<String, String> headers, String name) {
        if (headers == null || name == null) {
            return null;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (equalsName(entry.getKey(), name)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
