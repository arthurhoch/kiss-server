package io.github.arthurhoch.kiss.server.protocol.http11;

import io.github.arthurhoch.kiss.server.http.HttpHeaders;
import io.github.arthurhoch.kiss.server.http.HttpStatus;
import io.github.arthurhoch.kiss.server.http.Response;
import io.github.arthurhoch.kiss.server.routing.FastResponses;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Http11ResponseWriterTest {
    @Test
    void writesComputedContentLengthOnceWhenUserSuppliesContentLength() throws Exception {
        Response response = Response.text("OK").header("content-length", "999");
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        new Http11ResponseWriter().write(output, response);

        String raw = new String(output.toByteArray(), StandardCharsets.US_ASCII);
        String lower = raw.toLowerCase(Locale.ROOT);
        assertEquals(lower.indexOf("content-length:"), lower.lastIndexOf("content-length:"));
        assertTrue(raw.contains("Content-Length: 2\r\n"));
    }

    @Test
    void writesCommonStatusesAndExactBodyBytes() throws Exception {
        assertRaw(Response.text(HttpStatus.OK, "OK"), "HTTP/1.1 200 OK\r\n", "OK");
        assertRaw(Response.text(HttpStatus.CREATED, "created"), "HTTP/1.1 201 Created\r\n", "created");
        assertRaw(Response.text(HttpStatus.BAD_REQUEST, "bad"), "HTTP/1.1 400 Bad Request\r\n", "bad");
        assertRaw(Response.text(HttpStatus.NOT_FOUND, "missing"), "HTTP/1.1 404 Not Found\r\n", "missing");
        assertRaw(Response.text(HttpStatus.METHOD_NOT_ALLOWED, "no"), "HTTP/1.1 405 Method Not Allowed\r\n", "no");
        assertRaw(Response.text(HttpStatus.PAYLOAD_TOO_LARGE, "large"), "HTTP/1.1 413 Payload Too Large\r\n", "large");
        assertRaw(Response.text(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE, "headers"),
                "HTTP/1.1 431 Request Header Fields Too Large\r\n", "headers");
        assertRaw(Response.text(HttpStatus.INTERNAL_SERVER_ERROR, "error"),
                "HTTP/1.1 500 Internal Server Error\r\n", "error");
    }

    @Test
    void writesNoBodyForHeadAndNoContent() throws Exception {
        ByteArrayOutputStream headOutput = new ByteArrayOutputStream();
        new Http11ResponseWriter().write(headOutput, Response.text("OK"), false);
        String head = new String(headOutput.toByteArray(), StandardCharsets.US_ASCII);
        assertTrue(head.contains("Content-Length: 2\r\n"));
        assertTrue(head.endsWith("\r\n\r\n"));

        ByteArrayOutputStream noContentOutput = new ByteArrayOutputStream();
        new Http11ResponseWriter().write(noContentOutput, Response.text(HttpStatus.NO_CONTENT, "ignored"));
        String noContent = new String(noContentOutput.toByteArray(), StandardCharsets.US_ASCII);
        assertTrue(noContent.contains("HTTP/1.1 204 No Content\r\n"));
        assertTrue(noContent.contains("Content-Length: 0\r\n"));
        assertTrue(noContent.endsWith("\r\n\r\n"));
    }

    @Test
    void writesConnectionAndContentTypeHeaders() throws Exception {
        Response response = Response.text("OK").header(HttpHeaders.CONNECTION, "close");
        String raw = raw(response, true);

        assertTrue(raw.contains("Content-Type: text/plain; charset=utf-8\r\n"));
        assertTrue(raw.contains("Connection: close\r\n"));
        assertTrue(raw.contains("\r\n\r\nOK"));
    }

    @Test
    void writesFastResponseConnectionHeaderAndCanOmitBody() throws Exception {
        byte[] fast = FastResponses.text("OK");
        ByteArrayOutputStream keepAlive = new ByteArrayOutputStream();
        new Http11ResponseWriter().writeFast(keepAlive, fast, true, true);

        String raw = new String(keepAlive.toByteArray(), StandardCharsets.US_ASCII);
        assertTrue(raw.contains("Connection: keep-alive\r\n"));
        assertTrue(raw.endsWith("\r\n\r\nOK"));

        ByteArrayOutputStream head = new ByteArrayOutputStream();
        new Http11ResponseWriter().writeFast(head, fast, false, false);
        String headRaw = new String(head.toByteArray(), StandardCharsets.US_ASCII);
        assertTrue(headRaw.contains("Connection: close\r\n"));
        assertTrue(headRaw.contains("Content-Length: 2\r\n"));
        assertTrue(headRaw.endsWith("\r\n\r\n"));
    }

    private static void assertRaw(Response response, String statusLine, String body) throws Exception {
        String raw = raw(response, true);
        assertTrue(raw.startsWith(statusLine));
        assertTrue(raw.contains("Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n"));
        assertTrue(raw.endsWith("\r\n\r\n" + body));
    }

    private static String raw(Response response, boolean includeBody) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new Http11ResponseWriter().write(output, response, includeBody);
        return new String(output.toByteArray(), StandardCharsets.US_ASCII);
    }
}
