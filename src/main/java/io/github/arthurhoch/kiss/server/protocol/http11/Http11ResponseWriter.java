package io.github.arthurhoch.kiss.server.protocol.http11;

import io.github.arthurhoch.kiss.server.http.ContentType;
import io.github.arthurhoch.kiss.server.http.HttpHeaders;
import io.github.arthurhoch.kiss.server.http.HttpStatus;
import io.github.arthurhoch.kiss.server.http.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class Http11ResponseWriter {
    private static final byte[] CRLF = ascii("\r\n");
    private static final byte[] HEADER_END = ascii("\r\n\r\n");
    private static final byte[] HEADER_SEPARATOR = ascii(": ");
    private static final byte[] CONTENT_LENGTH_PREFIX = ascii("Content-Length: ");
    private static final byte[] CONTENT_TYPE_TEXT = ascii("Content-Type: " + ContentType.TEXT_PLAIN + "\r\n");
    private static final byte[] CONTENT_TYPE_JSON = ascii("Content-Type: " + ContentType.JSON + "\r\n");
    private static final byte[] CONNECTION_KEEP_ALIVE = ascii("Connection: keep-alive\r\n");
    private static final byte[] CONNECTION_CLOSE = ascii("Connection: close\r\n");
    private static final Map<HttpStatus, byte[]> STATUS_LINES = statusLines();

    private final byte[] numberScratch = new byte[20];

    public void write(OutputStream output, Response response) throws IOException {
        write(output, response, true);
    }

    public byte[] toBytes(Response response, boolean includeBody) {
        ByteArrayOutputStream output = new ByteArrayOutputStream(256 + response.bodyLength());
        try {
            write(output, response, includeBody);
        } catch (IOException e) {
            throw new IllegalStateException("could not encode in-memory response", e);
        }
        return output.toByteArray();
    }

    public void write(OutputStream output, Response response, boolean includeBody) throws IOException {
        Objects.requireNonNull(output, "output");
        Objects.requireNonNull(response, "response");
        boolean noContent = response.status() == HttpStatus.NO_CONTENT;
        int contentLength = noContent ? 0 : response.bodyLength();
        boolean writeBody = includeBody && !noContent && contentLength > 0;

        output.write(statusLine(response.status()));
        for (Map.Entry<String, String> entry : response.headers().entrySet()) {
            if (HttpHeaders.equalsName(entry.getKey(), HttpHeaders.CONTENT_LENGTH)) {
                continue;
            }
            writeHeader(output, entry.getKey(), entry.getValue());
        }
        output.write(CONTENT_LENGTH_PREFIX);
        writeDecimal(output, contentLength);
        output.write(CRLF);
        output.write(CRLF);
        if (writeBody) {
            response.writeBodyTo(output);
        }
    }

    public void writeFast(
            OutputStream output,
            byte[] response,
            boolean includeBody,
            boolean keepAlive
    ) throws IOException {
        Objects.requireNonNull(output, "output");
        Objects.requireNonNull(response, "response");
        int headerEnd = headerEnd(response);
        if (headerEnd < 0) {
            output.write(response);
            return;
        }
        int bodyStart = headerEnd + HEADER_END.length;
        boolean hasConnection = containsHeader(response, headerEnd, HttpHeaders.CONNECTION);
        if (hasConnection) {
            output.write(response, 0, includeBody ? response.length : bodyStart);
            return;
        }
        output.write(response, 0, headerEnd + 2);
        output.write(keepAlive ? CONNECTION_KEEP_ALIVE : CONNECTION_CLOSE);
        output.write(CRLF);
        if (includeBody && bodyStart < response.length) {
            output.write(response, bodyStart, response.length - bodyStart);
        }
    }

    private void writeHeader(OutputStream output, String name, String value) throws IOException {
        if (HttpHeaders.equalsName(name, HttpHeaders.CONTENT_TYPE)) {
            if (ContentType.TEXT_PLAIN.equals(value)) {
                output.write(CONTENT_TYPE_TEXT);
                return;
            }
            if (ContentType.JSON.equals(value)) {
                output.write(CONTENT_TYPE_JSON);
                return;
            }
        } else if (HttpHeaders.equalsName(name, HttpHeaders.CONNECTION)) {
            if ("keep-alive".equalsIgnoreCase(value)) {
                output.write(CONNECTION_KEEP_ALIVE);
                return;
            }
            if ("close".equalsIgnoreCase(value)) {
                output.write(CONNECTION_CLOSE);
                return;
            }
        }
        output.write(name.getBytes(StandardCharsets.US_ASCII));
        output.write(HEADER_SEPARATOR);
        output.write(value.getBytes(StandardCharsets.US_ASCII));
        output.write(CRLF);
    }

    private void writeDecimal(OutputStream output, int value) throws IOException {
        if (value == 0) {
            output.write('0');
            return;
        }
        int index = numberScratch.length;
        int current = value;
        while (current > 0) {
            numberScratch[--index] = (byte) ('0' + (current % 10));
            current /= 10;
        }
        output.write(numberScratch, index, numberScratch.length - index);
    }

    private static byte[] statusLine(HttpStatus status) {
        byte[] line = STATUS_LINES.get(status);
        if (line != null) {
            return line;
        }
        return ascii("HTTP/1.1 " + status.code() + " " + status.reason() + "\r\n");
    }

    private static Map<HttpStatus, byte[]> statusLines() {
        Map<HttpStatus, byte[]> lines = new EnumMap<>(HttpStatus.class);
        for (HttpStatus status : HttpStatus.values()) {
            lines.put(status, ascii("HTTP/1.1 " + status.code() + " " + status.reason() + "\r\n"));
        }
        return lines;
    }

    private static int headerEnd(byte[] response) {
        for (int i = 0; i <= response.length - HEADER_END.length; i++) {
            if (response[i] == '\r'
                    && response[i + 1] == '\n'
                    && response[i + 2] == '\r'
                    && response[i + 3] == '\n') {
                return i;
            }
        }
        return -1;
    }

    private static boolean containsHeader(byte[] response, int headerEnd, String name) {
        int lineStart = firstLineEnd(response, headerEnd);
        if (lineStart < 0) {
            return false;
        }
        lineStart += 2;
        while (lineStart < headerEnd) {
            int colon = lineStart;
            while (colon < headerEnd && response[colon] != ':' && response[colon] != '\r') {
                colon++;
            }
            if (colon < headerEnd && response[colon] == ':' && asciiEqualsIgnoreCase(response, lineStart, colon, name)) {
                return true;
            }
            int next = lineStart;
            while (next < headerEnd && !(response[next] == '\r' && next + 1 < headerEnd && response[next + 1] == '\n')) {
                next++;
            }
            lineStart = next + 2;
        }
        return false;
    }

    private static int firstLineEnd(byte[] response, int headerEnd) {
        for (int i = 0; i + 1 < headerEnd; i++) {
            if (response[i] == '\r' && response[i + 1] == '\n') {
                return i;
            }
        }
        return -1;
    }

    private static boolean asciiEqualsIgnoreCase(byte[] response, int start, int end, String expected) {
        if (end - start != expected.length()) {
            return false;
        }
        for (int i = 0; i < expected.length(); i++) {
            byte actual = response[start + i];
            char expectedChar = expected.charAt(i);
            if (actual >= 'A' && actual <= 'Z') {
                actual = (byte) (actual + ('a' - 'A'));
            }
            if (expectedChar >= 'A' && expectedChar <= 'Z') {
                expectedChar = (char) (expectedChar + ('a' - 'A'));
            }
            if (actual != expectedChar) {
                return false;
            }
        }
        return true;
    }

    private static byte[] ascii(String value) {
        return value.getBytes(StandardCharsets.US_ASCII);
    }
}
