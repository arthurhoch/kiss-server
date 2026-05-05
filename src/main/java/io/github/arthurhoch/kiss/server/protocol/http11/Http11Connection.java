package io.github.arthurhoch.kiss.server.protocol.http11;

import io.github.arthurhoch.kiss.server.ServerConfig;
import io.github.arthurhoch.kiss.server.http.Context;
import io.github.arthurhoch.kiss.server.http.HttpHeaders;
import io.github.arthurhoch.kiss.server.http.HttpMethod;
import io.github.arthurhoch.kiss.server.http.HttpStatus;
import io.github.arthurhoch.kiss.server.http.Request;
import io.github.arthurhoch.kiss.server.http.Response;
import io.github.arthurhoch.kiss.server.routing.Route;
import io.github.arthurhoch.kiss.server.routing.RouteMatch;
import io.github.arthurhoch.kiss.server.routing.Router;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.EnumSet;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Http11Connection implements Runnable {
    private static final int DEFAULT_MAX_HEADER_COUNT = 100;

    private final Socket socket;
    private final ServerConfig config;
    private final Router router;
    private final AtomicBoolean serverRunning;
    private final Runnable closeCallback;

    public Http11Connection(Socket socket) {
        this(socket, ServerConfig.defaults(), new Router(), new AtomicBoolean(true), () -> { });
    }

    public Http11Connection(
            Socket socket,
            ServerConfig config,
            Router router,
            AtomicBoolean serverRunning,
            Runnable closeCallback
    ) {
        this.socket = Objects.requireNonNull(socket, "socket");
        this.config = Objects.requireNonNull(config, "config");
        this.router = Objects.requireNonNull(router, "router");
        this.serverRunning = Objects.requireNonNull(serverRunning, "serverRunning");
        this.closeCallback = closeCallback == null ? () -> { } : closeCallback;
    }

    @Override
    public void run() {
        try (Socket ignored = socket) {
            socket.setSoTimeout(config.readTimeoutMillis());
            handleRequests();
        } catch (IOException ignored) {
            // Socket disconnects, timeouts, and broken pipes are normal network paths.
        } finally {
            closeCallback.run();
        }
    }

    private void handleRequests() throws IOException {
        Http11RequestParser parser = new Http11RequestParser(new Http11ParserLimits(
                config.maxRequestLineBytes(),
                config.maxHeaderBytes(),
                DEFAULT_MAX_HEADER_COUNT,
                config.maxBodyBytes()
        ), config.bufferSize());
        Http11ResponseWriter writer = new Http11ResponseWriter();
        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream();
        int handled = 0;

        while (serverRunning.get()) {
            try {
                ParsedRequest parsed = parser.parse(input);
                handled++;
                boolean keepAlive = shouldKeepAlive(parsed, handled);
                boolean fast = dispatch(parsed, output, writer, keepAlive);
                output.flush();
                if (!keepAlive || fast) {
                    return;
                }
                socket.setSoTimeout(config.idleTimeoutMillis());
            } catch (EOFException | SocketTimeoutException e) {
                return;
            } catch (Http11ParseException e) {
                writeError(writer, output, e.status());
                return;
            }
        }
    }

    private boolean dispatch(
            ParsedRequest parsed,
            OutputStream output,
            Http11ResponseWriter writer,
            boolean keepAlive
    ) throws IOException {
        Target target = splitTarget(parsed.target());
        RouteMatch match = router.matchOrNull(parsed.method(), target.path());
        if (match == null) {
            if (router.matchesPath(target.path())) {
                Response response = withConnection(
                        Response.text(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed")
                                .header("Allow", allowHeader(router.allowedMethods(target.path()))),
                        keepAlive
                );
                writer.write(output, response, parsed.method() != HttpMethod.HEAD);
            } else {
                writer.write(output, withConnection(Response.text(HttpStatus.NOT_FOUND, "Not Found"), keepAlive),
                        parsed.method() != HttpMethod.HEAD);
            }
            return false;
        }

        Route route = match.route();
        if (route.fast()) {
            try {
                writer.writeFast(output, route.directHandler().handle(), parsed.method() != HttpMethod.HEAD, keepAlive);
            } catch (Exception e) {
                writer.write(output, withConnection(Response.text(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"), false));
                return true;
            }
            return false;
        }

        Request request = new Request(parsed.method(), target.path(), target.query(), parsed.headers(), parsed.bodyUnsafe());
        Context context = new Context(request, match.pathParams());
        try {
            Response response = route.handler().handle(context);
            if (response == null) {
                response = Response.status(HttpStatus.NO_CONTENT);
            }
            writer.write(output, withConnection(response, keepAlive), parsed.method() != HttpMethod.HEAD);
        } catch (Exception e) {
            writer.write(output, withConnection(Response.text(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"), false));
            return false;
        }
        return false;
    }

    private boolean shouldKeepAlive(ParsedRequest parsed, int handled) {
        if (!config.keepAlive()) {
            return false;
        }
        if (handled >= config.maxKeepAliveRequests()) {
            return false;
        }
        String connection = HttpHeaders.get(parsed.headers(), HttpHeaders.CONNECTION);
        return connection == null || !equalsIgnoreCaseToken(connection, "close");
    }

    private static Response withConnection(Response response, boolean keepAlive) {
        return response.header(HttpHeaders.CONNECTION, keepAlive ? "keep-alive" : "close");
    }

    private static void writeError(Http11ResponseWriter writer, OutputStream output, HttpStatus status) throws IOException {
        writer.write(output, Response.text(status, status.reason()).header(HttpHeaders.CONNECTION, "close"));
        output.flush();
    }

    private static Target splitTarget(String target) throws IOException {
        int queryStart = target.indexOf('?');
        if (queryStart < 0) {
            return new Target(target, "");
        }
        return new Target(target.substring(0, queryStart), target.substring(queryStart + 1));
    }

    private static boolean equalsIgnoreCaseToken(String value, String token) {
        int start = 0;
        for (int i = 0; i <= value.length(); i++) {
            if (i == value.length() || value.charAt(i) == ',') {
                int left = start;
                int right = i;
                while (left < right && (value.charAt(left) == ' ' || value.charAt(left) == '\t')) {
                    left++;
                }
                while (right > left && (value.charAt(right - 1) == ' ' || value.charAt(right - 1) == '\t')) {
                    right--;
                }
                if (right - left == token.length() && value.regionMatches(true, left, token, 0, token.length())) {
                    return true;
                }
                start = i + 1;
            }
        }
        return false;
    }

    private static String allowHeader(EnumSet<HttpMethod> methods) {
        StringBuilder builder = new StringBuilder();
        for (HttpMethod method : methods) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(method.name());
        }
        return builder.toString();
    }

    private record Target(String path, String query) {
    }
}
