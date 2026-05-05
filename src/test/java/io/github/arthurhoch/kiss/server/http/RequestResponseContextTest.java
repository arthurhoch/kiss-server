package io.github.arthurhoch.kiss.server.http;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RequestResponseContextTest {
    @Test
    void requestDefensivelyCopiesBodyAndHeaders() {
        byte[] body = "hello".getBytes(StandardCharsets.UTF_8);
        Request request = new Request(HttpMethod.POST, "/echo", "a=1", Map.of("Content-Type", "text/plain"), body);
        body[0] = 'H';

        assertEquals(HttpMethod.POST, request.method());
        assertEquals("/echo", request.path());
        assertEquals("a=1", request.queryString());
        assertEquals("text/plain", request.header("content-type"));
        assertEquals("hello", request.bodyAsString());

        byte[] returned = request.body();
        returned[0] = 'H';
        assertArrayEquals("hello".getBytes(StandardCharsets.UTF_8), request.body());
    }

    @Test
    void contextExposesPathParamsAndResponseHelpers() {
        Request request = new Request(HttpMethod.GET, "/users/42", "", Map.of(), new byte[0]);
        Context context = new Context(request, Map.of("id", "42"));

        assertEquals(request, context.request());
        assertEquals("42", context.pathParam("id"));
        assertNull(context.pathParam("missing"));
        assertEquals("OK", new String(context.text("OK").body(), StandardCharsets.UTF_8));
    }

    @Test
    void responseValidatesHeadersAndContentType() {
        Response response = Response.text(HttpStatus.CREATED, "created").header("X-Test", "yes");

        assertEquals(HttpStatus.CREATED, response.status());
        assertEquals("yes", response.headers().get("X-Test"));
        assertEquals("created", new String(response.body(), StandardCharsets.UTF_8));
        assertThrows(IllegalArgumentException.class, () -> response.header("Bad Name", "x"));
        assertThrows(IllegalArgumentException.class, () -> response.header("Good", "x\r\nInjected: y"));
        assertThrows(IllegalArgumentException.class,
                () -> Response.body(HttpStatus.OK, null, "x", StandardCharsets.UTF_8));
    }
}
