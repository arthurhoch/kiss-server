package io.github.arthurhoch.kiss.server.protocol.http11;

import io.github.arthurhoch.kiss.server.http.HttpHeaders;
import io.github.arthurhoch.kiss.server.http.HttpMethod;
import io.github.arthurhoch.kiss.server.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Http11RequestParserTest {
    private final Http11ParserLimits limits = new Http11ParserLimits(128, 512, 8, 32);

    @Test
    void parsesValidRequests() throws Exception {
        ParsedRequest get = parse("GET /health HTTP/1.1\r\nHost: localhost\r\n\r\n");
        assertEquals(HttpMethod.GET, get.method());
        assertEquals("/health", get.target());
        assertEquals("localhost", get.headers().get("Host"));

        ParsedRequest query = parse("GET /users?id=1 HTTP/1.1\r\nHost: localhost\r\n\r\n");
        assertEquals("/users?id=1", query.target());

        ParsedRequest post = parse("POST /echo HTTP/1.1\r\nContent-Length: 5\r\n\r\nhello");
        assertEquals(HttpMethod.POST, post.method());
        assertArrayEquals("hello".getBytes(StandardCharsets.UTF_8), post.body());
    }

    @Test
    void preservesLeftoverBytesForNextKeepAliveRequest() throws Exception {
        String pipelined = "POST /echo HTTP/1.1\r\nContent-Length: 5\r\n\r\nhello"
                + "GET /health HTTP/1.1\r\nHost: localhost\r\n\r\n";
        Http11RequestParser parser = new Http11RequestParser(limits, 64);
        ByteArrayInputStream input = new NoSingleByteInputStream(pipelined.getBytes(StandardCharsets.US_ASCII));

        ParsedRequest first = parser.parse(input);
        ParsedRequest second = parser.parse(input);

        assertEquals(HttpMethod.POST, first.method());
        assertArrayEquals("hello".getBytes(StandardCharsets.US_ASCII), first.body());
        assertEquals(HttpMethod.GET, second.method());
        assertEquals("/health", second.target());
    }

    @Test
    void parsesLinesAcrossSmallInputBufferChunks() throws Exception {
        Http11RequestParser parser = new Http11RequestParser(limits, 7);
        ParsedRequest request = parser.parse(new NoSingleByteInputStream(
                "GET /chunked HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n"
                        .getBytes(StandardCharsets.US_ASCII)));

        assertEquals("/chunked", request.target());
        assertEquals("close", request.headers().get("Connection"));
    }

    @Test
    void trimsHeaderValuesAndKeepsMultipleHeaders() throws Exception {
        ParsedRequest request = parse("HEAD / HTTP/1.1\r\nHost:  localhost \r\nConnection:\tclose\t\r\n\r\n");

        assertEquals(HttpMethod.HEAD, request.method());
        assertEquals("localhost", request.headers().get("Host"));
        assertEquals("close", HttpHeaders.get(request.headers(), "connection"));
    }

    @Test
    void rejectsMalformedRequests() {
        assertThrows(EOFException.class, () -> parse(""));
        assertStatus(HttpStatus.BAD_REQUEST, "get / HTTP/1.1\r\n\r\n");
        assertStatus(HttpStatus.BAD_REQUEST, "GET / HTTP/1.0\r\n\r\n");
        assertStatus(HttpStatus.BAD_REQUEST, "GET / \r\n\r\n");
        assertStatus(HttpStatus.BAD_REQUEST, "GET / HTTP/1.1\n");
        assertStatus(HttpStatus.BAD_REQUEST, "GET / HTTP/1.1\r\nBad Header\r\n\r\n");
        assertStatus(HttpStatus.BAD_REQUEST, "GET / HTTP/1.1\r\nBad Name: x\r\n\r\n");
        assertStatus(HttpStatus.BAD_REQUEST, "GET / HTTP/1.1\r\nContent-Length: -1\r\n\r\n");
        assertStatus(HttpStatus.BAD_REQUEST, "GET / HTTP/1.1\r\nContent-Length: abc\r\n\r\n");
        assertStatus(HttpStatus.BAD_REQUEST, "GET / HTTP/1.1\r\nContent-Length:\r\n\r\n");
        assertStatus(HttpStatus.BAD_REQUEST,
                "GET / HTTP/1.1\r\nContent-Length: 1\r\nContent-Length: 2\r\n\r\n");
        assertStatus(HttpStatus.BAD_REQUEST, "GET / HTTP/1.1\r\n Transfer: folded\r\n\r\n");
        assertStatus(HttpStatus.BAD_REQUEST, "GET / HTTP/1.1\r\nTransfer-Encoding: chunked\r\n\r\n");
        assertStatus(HttpStatus.BAD_REQUEST, "POST / HTTP/1.1\r\nContent-Length: 5\r\n\r\nabc");
    }

    @Test
    void enforcesLimits() {
        assertStatus(new Http11ParserLimits(8, 512, 8, 32), HttpStatus.BAD_REQUEST,
                "GET /too-long HTTP/1.1\r\n\r\n");
        assertStatus(new Http11ParserLimits(128, 16, 8, 32), HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE,
                "GET / HTTP/1.1\r\nX-Test: 12345678901234567890\r\n\r\n");
        assertStatus(new Http11ParserLimits(128, 512, 1, 32), HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE,
                "GET / HTTP/1.1\r\nA: 1\r\nB: 2\r\n\r\n");
        assertStatus(HttpStatus.PAYLOAD_TOO_LARGE,
                "POST / HTTP/1.1\r\nContent-Length: 33\r\n\r\n");
    }

    private ParsedRequest parse(String text) throws Exception {
        return new Http11RequestParser(limits).parse(new NoSingleByteInputStream(text.getBytes(StandardCharsets.US_ASCII)));
    }

    private static void assertStatus(HttpStatus status, String text) {
        assertStatus(new Http11ParserLimits(128, 512, 8, 32), status, text);
    }

    private static void assertStatus(Http11ParserLimits limits, HttpStatus status, String text) {
        Http11RequestParser parser = new Http11RequestParser(limits);
        Http11ParseException error = assertThrows(Http11ParseException.class,
                () -> parser.parse(new NoSingleByteInputStream(text.getBytes(StandardCharsets.US_ASCII))));
        assertEquals(status, error.status());
    }

    private static final class NoSingleByteInputStream extends ByteArrayInputStream {
        private NoSingleByteInputStream(byte[] data) {
            super(data);
        }

        @Override
        public synchronized int read() {
            throw new AssertionError("parser must not use single-byte InputStream.read()");
        }

        @Override
        public synchronized int read(byte[] buffer, int offset, int length) {
            return super.read(buffer, offset, length);
        }
    }
}
