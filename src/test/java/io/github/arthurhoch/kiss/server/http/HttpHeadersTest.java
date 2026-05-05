package io.github.arthurhoch.kiss.server.http;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpHeadersTest {
    @Test
    void headerNamesAllowAsciiDigitsOnly() {
        assertDoesNotThrow(() -> HttpHeaders.requireValidName("X-Test-1"));
        assertThrows(IllegalArgumentException.class, () -> HttpHeaders.requireValidName("X-Test-\u0661"));
    }

    @Test
    void rejectsUnsafeHeaderNamesAndValues() {
        assertThrows(IllegalArgumentException.class, () -> HttpHeaders.requireValidName(""));
        assertThrows(IllegalArgumentException.class, () -> HttpHeaders.requireValidName("Bad Name"));
        assertThrows(IllegalArgumentException.class, () -> HttpHeaders.requireValidName("Bad:Name"));
        assertThrows(IllegalArgumentException.class, () -> HttpHeaders.requireSafeValue("ok\r\nInjected: yes"));
    }

    @Test
    void looksUpHeadersCaseInsensitively() {
        Map<String, String> headers = Map.of("content-type", "text/plain");

        assertEquals("text/plain", HttpHeaders.get(headers, "Content-Type"));
        assertTrue(HttpHeaders.contains(headers, "CONTENT-TYPE"));
        assertNull(HttpHeaders.get(headers, "Missing"));
    }
}
