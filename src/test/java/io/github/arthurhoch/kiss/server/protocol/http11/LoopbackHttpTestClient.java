package io.github.arthurhoch.kiss.server.protocol.http11;

import io.github.arthurhoch.kiss.server.ServerHandle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test-only helper for intentional cleartext loopback probes.
 *
 * <p>The production server being tested is an HTTP/1.1 server, so these tests
 * must use plain localhost sockets to verify protocol behavior.</p>
 */
final class LoopbackHttpTestClient {
    private static final String LOOPBACK_HOST = "127.0.0.1";
    private static final String LOOPBACK_URI_PREFIX = "http://" + LOOPBACK_HOST + ":";
    private static final int DEFAULT_SOCKET_TIMEOUT_MILLIS = 2_000;

    private LoopbackHttpTestClient() {
    }

    static HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }

    static HttpClient httpClient(Duration connectTimeout) {
        return HttpClient.newBuilder().connectTimeout(connectTimeout).build();
    }

    static RawConnection connect(ServerHandle handle) throws IOException {
        return new RawConnection(handle);
    }

    static HttpResponse<String> send(
            HttpClient client,
            ServerHandle handle,
            String method,
            String path,
            String body
    ) throws IOException, InterruptedException {
        HttpRequest.BodyPublisher publisher = body.isEmpty()
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body);
        HttpRequest request = HttpRequest.newBuilder(loopbackUri(handle, path))
                .method(method, publisher)
                .header("Content-Type", "text/plain")
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static URI loopbackUri(ServerHandle handle, String path) {
        int port = handle.port();
        if (port <= 0 || port > 65_535) {
            throw new IllegalArgumentException("server handle does not expose a valid port");
        }
        if (!path.startsWith("/") || path.contains("://") || path.indexOf('\r') >= 0 || path.indexOf('\n') >= 0) {
            throw new IllegalArgumentException("path must be a local absolute HTTP path");
        }
        return URI.create(LOOPBACK_URI_PREFIX + port + path);
    }

    static final class RawConnection implements AutoCloseable {
        private final Socket socket;

        private RawConnection(ServerHandle handle) throws IOException {
            this.socket = new Socket(LOOPBACK_HOST, handle.port());
            this.socket.setSoTimeout(DEFAULT_SOCKET_TIMEOUT_MILLIS);
        }

        void write(String request) throws IOException {
            OutputStream output = socket.getOutputStream();
            output.write(request.getBytes(StandardCharsets.US_ASCII));
            output.flush();
        }

        void writeAscii(byte[] request) throws IOException {
            OutputStream output = socket.getOutputStream();
            output.write(request);
            output.flush();
        }

        RawResponse readResponse() throws IOException {
            return LoopbackHttpTestClient.readResponse(socket.getInputStream());
        }

        int readByte() throws IOException {
            return socket.getInputStream().read();
        }

        void setSoTimeout(int timeoutMillis) throws SocketException {
            socket.setSoTimeout(timeoutMillis);
        }

        void assertClosedOrReset() throws IOException {
            try {
                assertEquals(-1, readByte());
            } catch (SocketException expected) {
                String message = expected.getMessage();
                assertTrue(message == null || message.contains("reset") || message.contains("closed"));
            }
        }

        @Override
        public void close() throws IOException {
            socket.close();
        }
    }

    private static RawResponse readResponse(InputStream input) throws IOException {
        String head = readHead(input);
        int lineEnd = head.indexOf("\r\n");
        int firstSpace = head.indexOf(' ');
        int secondSpace = head.indexOf(' ', firstSpace + 1);
        if (lineEnd < 0 || firstSpace < 0 || secondSpace <= firstSpace) {
            throw new IOException("invalid response status line");
        }
        int status = parseInt(head.substring(firstSpace + 1, secondSpace), "status");
        Map<String, String> headers = parseHeaders(head.substring(lineEnd + 2));
        int contentLength = headers.containsKey("content-length")
                ? parseInt(headers.get("content-length"), "content-length")
                : 0;
        byte[] body = input.readNBytes(contentLength);
        return new RawResponse(status, headers, new String(body, StandardCharsets.UTF_8));
    }

    private static int parseInt(String value, String field) throws IOException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IOException("invalid response " + field + ": " + value, e);
        }
    }

    private static String readHead(InputStream input) throws IOException {
        byte[] buffer = new byte[8192];
        int count = 0;
        while (true) {
            int value = input.read();
            if (value < 0) {
                throw new IOException("connection closed before response head");
            }
            buffer[count++] = (byte) value;
            if (count >= 4
                    && buffer[count - 4] == '\r'
                    && buffer[count - 3] == '\n'
                    && buffer[count - 2] == '\r'
                    && buffer[count - 1] == '\n') {
                return new String(buffer, 0, count, StandardCharsets.US_ASCII);
            }
            if (count == buffer.length) {
                throw new IOException("response head too large");
            }
        }
    }

    private static Map<String, String> parseHeaders(String text) {
        Map<String, String> headers = new LinkedHashMap<>();
        int start = 0;
        while (start < text.length()) {
            int end = text.indexOf("\r\n", start);
            if (end < 0 || end == start) {
                return headers;
            }
            int colon = text.indexOf(':', start);
            headers.put(text.substring(start, colon).toLowerCase(Locale.ROOT),
                    text.substring(colon + 1, end).trim());
            start = end + 2;
        }
        return headers;
    }

    record RawResponse(int statusCode, Map<String, String> headers, String body) {
        String header(String name) {
            return headers.get(name.toLowerCase(Locale.ROOT));
        }
    }
}
