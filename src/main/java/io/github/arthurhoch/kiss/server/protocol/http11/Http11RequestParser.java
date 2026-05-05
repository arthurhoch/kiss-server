package io.github.arthurhoch.kiss.server.protocol.http11;

import io.github.arthurhoch.kiss.server.http.HttpHeaders;
import io.github.arthurhoch.kiss.server.http.HttpMethod;
import io.github.arthurhoch.kiss.server.http.HttpStatus;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class Http11RequestParser {
    private static final int DEFAULT_INPUT_BUFFER_SIZE = 16 * 1024;
    private static final byte[] EMPTY_BODY = new byte[0];

    private final Http11ParserLimits limits;
    private final byte[] inputBuffer;
    private final byte[] lineBuffer;
    private int position;
    private int limit;

    public Http11RequestParser(Http11ParserLimits limits) {
        this(limits, DEFAULT_INPUT_BUFFER_SIZE);
    }

    public Http11RequestParser(Http11ParserLimits limits, int inputBufferSize) {
        this.limits = Objects.requireNonNull(limits, "limits");
        if (inputBufferSize <= 0) {
            throw new IllegalArgumentException("inputBufferSize must be positive");
        }
        this.inputBuffer = new byte[inputBufferSize];
        this.lineBuffer = new byte[Math.max(limits.maxRequestLineBytes(), limits.maxHeaderBytes()) + 2];
    }

    public Http11ParserLimits limits() {
        return limits;
    }

    public ParsedRequest parse(InputStream input) throws IOException, Http11ParseException {
        Objects.requireNonNull(input, "input");
        int requestLineLength = readHttpLine(input, limits.maxRequestLineBytes(), HttpStatus.BAD_REQUEST);
        if (requestLineLength == 0) {
            throw badRequest("empty request line");
        }

        RequestLine parsedLine = parseRequestLine(requestLineLength);
        Map<String, String> headers = readHeaders(input);
        int contentLength = contentLength(headers);
        byte[] body = readBody(input, contentLength);
        return ParsedRequest.trusted(parsedLine.method(), parsedLine.target(), parsedLine.version(), headers, body);
    }

    private Map<String, String> readHeaders(InputStream input) throws IOException, Http11ParseException {
        Map<String, String> headers = new LinkedHashMap<>();
        int headerBytes = 0;
        int headerCount = 0;
        while (true) {
            int remaining = limits.maxHeaderBytes() - headerBytes;
            if (remaining <= 0) {
                throw new Http11ParseException(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE, "headers too large");
            }
            int lineLength = readHttpLine(input, remaining, HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE);
            headerBytes += lineLength + 2;
            if (headerBytes > limits.maxHeaderBytes()) {
                throw new Http11ParseException(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE, "headers too large");
            }
            if (lineLength == 0) {
                return headers;
            }
            headerCount++;
            if (headerCount > limits.maxHeaderCount()) {
                throw new Http11ParseException(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE, "too many headers");
            }
            addHeader(headers, lineLength);
        }
    }

    private int readHttpLine(InputStream input, int maxBytes, HttpStatus tooLargeStatus)
            throws IOException, Http11ParseException {
        int count = 0;
        boolean previousWasCarriageReturn = false;
        while (true) {
            if (position == limit) {
                fill(input, count);
            }
            while (position < limit) {
                byte value = inputBuffer[position++];
                if (previousWasCarriageReturn && value != '\n') {
                    throw badRequest("invalid CRLF");
                }
                if (count == lineBuffer.length) {
                    throw new Http11ParseException(tooLargeStatus, "line too large");
                }
                lineBuffer[count++] = value;
                if (count > maxBytes && value != '\r' && value != '\n') {
                    throw new Http11ParseException(tooLargeStatus, "line too large");
                }
                if (count > maxBytes + 2) {
                    throw new Http11ParseException(tooLargeStatus, "line too large");
                }
                if (value == '\n') {
                    if (!previousWasCarriageReturn) {
                        throw badRequest("invalid CRLF");
                    }
                    return count - 2;
                }
                previousWasCarriageReturn = value == '\r';
            }
        }
    }

    private void fill(InputStream input, int currentLineBytes) throws IOException, Http11ParseException {
        int read;
        do {
            read = input.read(inputBuffer, 0, inputBuffer.length);
        } while (read == 0);
        if (read < 0) {
            if (currentLineBytes == 0) {
                throw new EOFException("end of stream");
            }
            throw badRequest("unexpected end of line");
        }
        position = 0;
        limit = read;
    }

    private RequestLine parseRequestLine(int length) throws Http11ParseException {
        int firstSpace = indexOfSpace(0, length);
        int secondSpace = firstSpace < 0 ? -1 : indexOfSpace(firstSpace + 1, length);
        if (firstSpace <= 0 || secondSpace <= firstSpace + 1 || secondSpace >= length - 1) {
            throw badRequest("invalid request line");
        }
        if (indexOfSpace(secondSpace + 1, length) >= 0) {
            throw badRequest("invalid request line");
        }
        HttpMethod method = parseMethod(0, firstSpace);
        int targetStart = firstSpace + 1;
        int targetLength = secondSpace - targetStart;
        if (targetLength == 0 || lineBuffer[targetStart] != '/') {
            throw badRequest("request target must be origin-form");
        }
        if (!matchesAscii(secondSpace + 1, length - secondSpace - 1, "HTTP/1.1")) {
            throw badRequest("unsupported HTTP version");
        }
        String target = new String(lineBuffer, targetStart, targetLength, StandardCharsets.US_ASCII);
        return new RequestLine(method, target, "HTTP/1.1");
    }

    private int indexOfSpace(int start, int end) {
        for (int i = start; i < end; i++) {
            if (lineBuffer[i] == ' ') {
                return i;
            }
        }
        return -1;
    }

    private void addHeader(Map<String, String> headers, int length) throws Http11ParseException {
        if (lineBuffer[0] == ' ' || lineBuffer[0] == '\t') {
            throw badRequest("folded headers are not supported");
        }
        int colon = -1;
        for (int i = 0; i < length; i++) {
            if (lineBuffer[i] == ':') {
                colon = i;
                break;
            }
        }
        if (colon <= 0) {
            throw badRequest("malformed header");
        }
        for (int i = 0; i < colon; i++) {
            if (!isHeaderNameByte(lineBuffer[i])) {
                throw badRequest("invalid header name");
            }
        }
        int valueStart = colon + 1;
        int valueEnd = length;
        while (valueStart < valueEnd && isOptionalWhitespace(lineBuffer[valueStart])) {
            valueStart++;
        }
        while (valueEnd > valueStart && isOptionalWhitespace(lineBuffer[valueEnd - 1])) {
            valueEnd--;
        }

        String name = new String(lineBuffer, 0, colon, StandardCharsets.US_ASCII);
        String value = new String(lineBuffer, valueStart, valueEnd - valueStart, StandardCharsets.US_ASCII);
        if (HttpHeaders.equalsName(name, HttpHeaders.TRANSFER_ENCODING)) {
            throw badRequest("Transfer-Encoding is not supported");
        }
        String existingName = existingHeaderName(headers, name);
        if (existingName != null && HttpHeaders.equalsName(name, HttpHeaders.CONTENT_LENGTH)) {
            String existing = headers.get(existingName);
            if (!existing.equals(value)) {
                throw badRequest("conflicting Content-Length");
            }
            return;
        }
        if (existingName != null) {
            headers.put(existingName, headers.get(existingName) + ", " + value);
        } else {
            headers.put(name, value);
        }
    }

    private int contentLength(Map<String, String> headers) throws Http11ParseException {
        String value = HttpHeaders.get(headers, HttpHeaders.CONTENT_LENGTH);
        if (value == null) {
            return 0;
        }
        if (value.isEmpty()) {
            throw badRequest("invalid Content-Length");
        }
        long length = 0;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                throw badRequest("invalid Content-Length");
            }
            length = (length * 10) + (c - '0');
            if (length > limits.maxBodyBytes() || length > Integer.MAX_VALUE) {
                throw new Http11ParseException(HttpStatus.PAYLOAD_TOO_LARGE, "body too large");
            }
        }
        return (int) length;
    }

    private byte[] readBody(InputStream input, int contentLength) throws IOException, Http11ParseException {
        if (contentLength == 0) {
            return EMPTY_BODY;
        }
        byte[] body = new byte[contentLength];
        int copied = Math.min(contentLength, limit - position);
        if (copied > 0) {
            System.arraycopy(inputBuffer, position, body, 0, copied);
            position += copied;
        }
        int offset = copied;
        while (offset < contentLength) {
            int read = input.read(body, offset, contentLength - offset);
            if (read < 0) {
                throw badRequest("unexpected disconnect while reading body");
            }
            if (read > 0) {
                offset += read;
            }
        }
        return body;
    }

    private static boolean isHeaderNameByte(byte value) {
        return value == '!' || value == '#' || value == '$' || value == '%' || value == '&'
                || value == '\'' || value == '*' || value == '+' || value == '-' || value == '.'
                || value == '^' || value == '_' || value == '`' || value == '|' || value == '~'
                || (value >= '0' && value <= '9')
                || (value >= 'A' && value <= 'Z')
                || (value >= 'a' && value <= 'z');
    }

    private static boolean isOptionalWhitespace(byte value) {
        return value == ' ' || value == '\t';
    }

    private boolean matchesAscii(int start, int length, String expected) {
        if (length != expected.length()) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (lineBuffer[start + i] != expected.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    private HttpMethod parseMethod(int start, int end) throws Http11ParseException {
        int length = end - start;
        if (length == 3 && lineBuffer[start] == 'G' && lineBuffer[start + 1] == 'E' && lineBuffer[start + 2] == 'T') {
            return HttpMethod.GET;
        }
        if (length == 4 && lineBuffer[start] == 'P' && lineBuffer[start + 1] == 'O'
                && lineBuffer[start + 2] == 'S' && lineBuffer[start + 3] == 'T') {
            return HttpMethod.POST;
        }
        if (length == 3 && lineBuffer[start] == 'P' && lineBuffer[start + 1] == 'U' && lineBuffer[start + 2] == 'T') {
            return HttpMethod.PUT;
        }
        if (length == 6 && lineBuffer[start] == 'D' && lineBuffer[start + 1] == 'E'
                && lineBuffer[start + 2] == 'L' && lineBuffer[start + 3] == 'E'
                && lineBuffer[start + 4] == 'T' && lineBuffer[start + 5] == 'E') {
            return HttpMethod.DELETE;
        }
        if (length == 5 && lineBuffer[start] == 'P' && lineBuffer[start + 1] == 'A'
                && lineBuffer[start + 2] == 'T' && lineBuffer[start + 3] == 'C'
                && lineBuffer[start + 4] == 'H') {
            return HttpMethod.PATCH;
        }
        if (length == 4 && lineBuffer[start] == 'H' && lineBuffer[start + 1] == 'E'
                && lineBuffer[start + 2] == 'A' && lineBuffer[start + 3] == 'D') {
            return HttpMethod.HEAD;
        }
        if (length == 7 && lineBuffer[start] == 'O' && lineBuffer[start + 1] == 'P'
                && lineBuffer[start + 2] == 'T' && lineBuffer[start + 3] == 'I'
                && lineBuffer[start + 4] == 'O' && lineBuffer[start + 5] == 'N'
                && lineBuffer[start + 6] == 'S') {
            return HttpMethod.OPTIONS;
        }
        throw badRequest("unsupported HTTP method");
    }

    private static String existingHeaderName(Map<String, String> headers, String name) {
        for (String existing : headers.keySet()) {
            if (existing.equalsIgnoreCase(name)) {
                return existing;
            }
        }
        return null;
    }

    private static Http11ParseException badRequest(String message) {
        return new Http11ParseException(HttpStatus.BAD_REQUEST, message);
    }

    private record RequestLine(HttpMethod method, String target, String version) {
    }
}
