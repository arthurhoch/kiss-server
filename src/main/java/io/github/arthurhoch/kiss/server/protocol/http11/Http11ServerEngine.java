package io.github.arthurhoch.kiss.server.protocol.http11;

import io.github.arthurhoch.kiss.server.HandlerExecutionMode;
import io.github.arthurhoch.kiss.server.ServerConfig;
import io.github.arthurhoch.kiss.server.ServerHandle;
import io.github.arthurhoch.kiss.server.http.Context;
import io.github.arthurhoch.kiss.server.http.HttpHeaders;
import io.github.arthurhoch.kiss.server.http.HttpMethod;
import io.github.arthurhoch.kiss.server.http.HttpStatus;
import io.github.arthurhoch.kiss.server.http.Request;
import io.github.arthurhoch.kiss.server.http.Response;
import io.github.arthurhoch.kiss.server.routing.FastResponses;
import io.github.arthurhoch.kiss.server.routing.Route;
import io.github.arthurhoch.kiss.server.routing.RouteMatch;
import io.github.arthurhoch.kiss.server.routing.Router;
import io.github.arthurhoch.kiss.server.runtime.ConnectionLimiter;
import io.github.arthurhoch.kiss.server.runtime.ExecutorStrategy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Http11ServerEngine {
    private static final int DEFAULT_MAX_HEADER_COUNT = 100;
    private static final int SELECT_TIMEOUT_MILLIS = 100;
    private static final long MAX_PENDING_WRITE_BYTES = 8L * 1024 * 1024;

    private final ServerConfig config;
    private final Router router;
    private final Http11ParserLimits limits;

    public Http11ServerEngine(ServerConfig config, Router router) {
        this.config = Objects.requireNonNull(config, "config");
        this.router = Objects.requireNonNull(router, "router");
        this.limits = new Http11ParserLimits(
                config.maxRequestLineBytes(),
                config.maxHeaderBytes(),
                DEFAULT_MAX_HEADER_COUNT,
                config.maxBodyBytes()
        );
    }

    public ServerConfig config() {
        return config;
    }

    public Router router() {
        return router;
    }

    public ServerHandle start() {
        try {
            Selector selector = Selector.open();
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            serverChannel.bind(new InetSocketAddress(InetAddress.getByName(config.host()), config.port()), 1024);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            ExecutorStrategy executorStrategy = ExecutorStrategy.from(config);
            ConnectionLimiter limiter = new ConnectionLimiter(config.maxConnections());
            AtomicBoolean running = new AtomicBoolean(true);
            Set<ConnectionState> connections = ConcurrentHashMap.newKeySet();
            ConcurrentLinkedQueue<Runnable> selectorTasks = new ConcurrentLinkedQueue<>();
            Thread loopThread = new Thread(
                    () -> eventLoop(serverChannel, selector, executorStrategy, limiter, running, connections, selectorTasks),
                    "kiss-server-nio-" + serverChannel.socket().getLocalPort()
            );
            loopThread.setDaemon(true);
            loopThread.start();
            return new EngineHandle(serverChannel, selector, executorStrategy, running, connections, selectorTasks, loopThread);
        } catch (IOException e) {
            throw new IllegalStateException("failed to start KissServer on " + config.host() + ":" + config.port(), e);
        }
    }

    private void eventLoop(
            ServerSocketChannel serverChannel,
            Selector selector,
            ExecutorStrategy executorStrategy,
            ConnectionLimiter limiter,
            AtomicBoolean running,
            Set<ConnectionState> connections,
            ConcurrentLinkedQueue<Runnable> selectorTasks
    ) {
        try {
            while (running.get()) {
                selector.select(SELECT_TIMEOUT_MILLIS);
                runSelectorTasks(selectorTasks);
                var selected = selector.selectedKeys().iterator();
                while (selected.hasNext()) {
                    SelectionKey key = selected.next();
                    selected.remove();
                    if (!key.isValid()) {
                        continue;
                    }
                    try {
                        if (key.isAcceptable()) {
                            accept(serverChannel, selector, limiter, connections);
                        }
                        if (key.isValid() && key.isReadable()) {
                            read((ConnectionState) key.attachment(), executorStrategy, selectorTasks, selector);
                        }
                        if (key.isValid() && key.isWritable()) {
                            write((ConnectionState) key.attachment());
                        }
                    } catch (CancelledKeyException ignored) {
                    }
                }
                closeIdleConnections(connections);
            }
        } catch (IOException ignored) {
        } finally {
            closeAll(connections);
            closeQuietly(serverChannel);
            closeQuietly(selector);
        }
    }

    private static void runSelectorTasks(ConcurrentLinkedQueue<Runnable> selectorTasks) {
        Runnable task;
        while ((task = selectorTasks.poll()) != null) {
            task.run();
        }
    }

    private void accept(
            ServerSocketChannel serverChannel,
            Selector selector,
            ConnectionLimiter limiter,
            Set<ConnectionState> connections
    ) throws IOException {
        SocketChannel channel;
        while ((channel = serverChannel.accept()) != null) {
            if (!limiter.tryAcquire()) {
                writeBusyAndClose(channel);
                continue;
            }
            channel.configureBlocking(false);
            channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
            int inputCapacity = Math.max(config.bufferSize(),
                    config.maxRequestLineBytes() + config.maxHeaderBytes() + 4);
            ConnectionState state = new ConnectionState(channel, limiter, connections, inputCapacity);
            SelectionKey key = channel.register(selector, SelectionKey.OP_READ, state);
            state.key = key;
            connections.add(state);
        }
    }

    private void read(
            ConnectionState state,
            ExecutorStrategy executorStrategy,
            ConcurrentLinkedQueue<Runnable> selectorTasks,
            Selector selector
    ) {
        if (state.closed || state.handlerInProgress) {
            return;
        }
        try {
            int totalRead = 0;
            while (state.hasInputSpace()) {
                state.readView.position(state.writePosition);
                state.readView.limit(state.input.length);
                int read = state.channel.read(state.readView);
                if (read < 0) {
                    close(state);
                    return;
                }
                if (read == 0) {
                    break;
                }
                totalRead += read;
                state.writePosition += read;
                state.lastActivityMillis = System.currentTimeMillis();
            }
            if (totalRead == 0 && !state.hasInputSpace()) {
                fail(state, HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE);
                return;
            }
            parseAvailable(state, executorStrategy, selectorTasks, selector);
        } catch (IOException e) {
            close(state);
        }
    }

    private void parseAvailable(
            ConnectionState state,
            ExecutorStrategy executorStrategy,
            ConcurrentLinkedQueue<Runnable> selectorTasks,
            Selector selector
    ) {
        while (!state.closed && !state.handlerInProgress) {
            ParsedRequest parsed;
            try {
                parsed = state.parser.parse(state);
            } catch (Http11ParseException e) {
                fail(state, e.status());
                return;
            }
            if (parsed == null) {
                state.compactIfNeeded();
                return;
            }
            state.handledRequests++;
            handleParsedRequest(state, parsed, executorStrategy, selectorTasks, selector);
            if (state.handlerInProgress || state.closeAfterWrite) {
                state.compactIfNeeded();
                return;
            }
        }
    }

    private void handleParsedRequest(
            ConnectionState state,
            ParsedRequest parsed,
            ExecutorStrategy executorStrategy,
            ConcurrentLinkedQueue<Runnable> selectorTasks,
            Selector selector
    ) {
        boolean keepAlive = shouldKeepAlive(parsed, state.handledRequests);
        Target target = splitTarget(parsed.target());
        RouteMatch match = router.matchOrNull(parsed.method(), target.path());
        if (match == null) {
            if (router.matchesPath(target.path())) {
                Response response = withConnection(
                        Response.text(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed")
                                .header("Allow", allowHeader(router.allowedMethods(target.path()))),
                        keepAlive
                );
                enqueueResponse(state, responseBytes(response, parsed.method(), true), !keepAlive);
            } else {
                Response response = withConnection(Response.text(HttpStatus.NOT_FOUND, "Not Found"), keepAlive);
                enqueueResponse(state, responseBytes(response, parsed.method(), true), !keepAlive);
            }
            return;
        }

        Route route = match.route();
        if (route.fast()) {
            try {
                enqueueResponse(state, fastBytes(route, parsed.method(), keepAlive), !keepAlive);
            } catch (Exception e) {
                enqueueResponse(state, responseBytes(internalError(false), parsed.method(), true), true);
            }
            return;
        }

        if (config.handlerExecutionMode() == HandlerExecutionMode.DIRECT) {
            handleNormalRoute(state, parsed, target, match, keepAlive);
            return;
        }

        state.handlerInProgress = true;
        disableInterest(state, SelectionKey.OP_READ);
        try {
            executorStrategy.executor().execute(() -> {
                byte[] responseBytes;
                boolean closeAfter;
                try {
                    responseBytes = normalResponseBytes(parsed, target, match, keepAlive);
                    closeAfter = !keepAlive;
                } catch (Exception e) {
                    responseBytes = responseBytes(internalError(false), parsed.method(), true);
                    closeAfter = true;
                }
                byte[] encoded = responseBytes;
                boolean finalCloseAfter = closeAfter;
                selectorTasks.add(() -> completeWorkerResponse(state, encoded, finalCloseAfter, executorStrategy, selectorTasks, selector));
                selector.wakeup();
            });
        } catch (RejectedExecutionException e) {
            close(state);
        }
    }

    private void completeWorkerResponse(
            ConnectionState state,
            byte[] responseBytes,
            boolean closeAfter,
            ExecutorStrategy executorStrategy,
            ConcurrentLinkedQueue<Runnable> selectorTasks,
            Selector selector
    ) {
        if (state.closed) {
            return;
        }
        state.handlerInProgress = false;
        enqueueResponse(state, responseBytes, closeAfter);
        if (!closeAfter) {
            enableInterest(state, SelectionKey.OP_READ);
            parseAvailable(state, executorStrategy, selectorTasks, selector);
        }
    }

    private void handleNormalRoute(
            ConnectionState state,
            ParsedRequest parsed,
            Target target,
            RouteMatch match,
            boolean keepAlive
    ) {
        try {
            enqueueResponse(state, normalResponseBytes(parsed, target, match, keepAlive), !keepAlive);
        } catch (Exception e) {
            enqueueResponse(state, responseBytes(internalError(false), parsed.method(), true), true);
        }
    }

    private byte[] normalResponseBytes(
            ParsedRequest parsed,
            Target target,
            RouteMatch match,
            boolean keepAlive
    ) throws Exception {
        Request request = new Request(parsed.method(), target.path(), target.query(), parsed.headers(), parsed.bodyUnsafe());
        Context context = new Context(request, match.pathParams());
        Response response = match.route().handler().handle(context);
        if (response == null) {
            response = Response.status(HttpStatus.NO_CONTENT);
        }
        return responseBytes(withConnection(response, keepAlive), parsed.method(), true);
    }

    private byte[] responseBytes(Response response, HttpMethod method, boolean includeHeadBodyRule) {
        boolean includeBody = !includeHeadBodyRule || method != HttpMethod.HEAD;
        return new Http11ResponseWriter().toBytes(response, includeBody);
    }

    private byte[] fastBytes(Route route, HttpMethod method, boolean keepAlive) throws Exception {
        byte[] response = route.hasPrebuiltDirectResponse()
                ? route.directResponseUnsafe()
                : route.directHandler().handle();
        return FastResponses.withConnection(response, method != HttpMethod.HEAD, keepAlive);
    }

    private void enqueueResponse(ConnectionState state, byte[] response, boolean closeAfterWrite) {
        if (state.closed) {
            return;
        }
        if (state.pendingWriteBytes + response.length > MAX_PENDING_WRITE_BYTES) {
            close(state);
            return;
        }
        state.pendingWrites.add(ByteBuffer.wrap(response));
        state.pendingWriteBytes += response.length;
        state.closeAfterWrite = state.closeAfterWrite || closeAfterWrite;
        if (state.closeAfterWrite) {
            disableInterest(state, SelectionKey.OP_READ);
        }
        enableInterest(state, SelectionKey.OP_WRITE);
    }

    private void write(ConnectionState state) {
        if (state.closed) {
            return;
        }
        try {
            while (!state.pendingWrites.isEmpty()) {
                ByteBuffer current = state.pendingWrites.peek();
                int before = current.remaining();
                int written = state.channel.write(current);
                if (written < 0) {
                    close(state);
                    return;
                }
                state.pendingWriteBytes -= before - current.remaining();
                if (current.hasRemaining()) {
                    break;
                }
                state.pendingWrites.remove();
            }
            state.lastActivityMillis = System.currentTimeMillis();
            if (state.pendingWrites.isEmpty()) {
                disableInterest(state, SelectionKey.OP_WRITE);
                if (state.closeAfterWrite) {
                    close(state);
                }
            }
        } catch (IOException e) {
            close(state);
        }
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

    private void fail(ConnectionState state, HttpStatus status) {
        enqueueResponse(state, responseBytes(withConnection(Response.text(status, status.reason()), false), HttpMethod.GET, false), true);
    }

    private static Response withConnection(Response response, boolean keepAlive) {
        return response.header(HttpHeaders.CONNECTION, keepAlive ? "keep-alive" : "close");
    }

    private static Response internalError(boolean keepAlive) {
        return withConnection(Response.text(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"), keepAlive);
    }

    private static Target splitTarget(String target) {
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

    private void closeIdleConnections(Set<ConnectionState> connections) {
        long now = System.currentTimeMillis();
        for (ConnectionState state : connections) {
            long timeoutMillis = state.handledRequests == 0 ? config.readTimeoutMillis() : config.idleTimeoutMillis();
            if (now - state.lastActivityMillis > timeoutMillis) {
                close(state);
            }
        }
    }

    private static void enableInterest(ConnectionState state, int operation) {
        if (state.closed || state.key == null || !state.key.isValid()) {
            return;
        }
        state.key.interestOps(state.key.interestOps() | operation);
    }

    private static void disableInterest(ConnectionState state, int operation) {
        if (state.closed || state.key == null || !state.key.isValid()) {
            return;
        }
        state.key.interestOps(state.key.interestOps() & ~operation);
    }

    private static void close(ConnectionState state) {
        if (state.closed) {
            return;
        }
        state.closed = true;
        state.connections.remove(state);
        state.limiter.release();
        if (state.key != null) {
            state.key.cancel();
        }
        closeQuietly(state.channel);
    }

    private static void closeAll(Set<ConnectionState> connections) {
        for (ConnectionState state : connections) {
            close(state);
        }
    }

    private static void writeBusyAndClose(SocketChannel channel) {
        try (SocketChannel close = channel) {
            close.configureBlocking(false);
            ByteBuffer response = ByteBuffer.wrap(FastResponses.withConnection(
                    FastResponses.text(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable"), true, false));
            while (response.hasRemaining()) {
                if (close.write(response) == 0) {
                    break;
                }
            }
        } catch (IOException ignored) {
        }
    }

    private static void closeQuietly(ServerSocketChannel channel) {
        try {
            channel.close();
        } catch (IOException ignored) {
        }
    }

    private static void closeQuietly(SocketChannel channel) {
        try {
            channel.close();
        } catch (IOException ignored) {
        }
    }

    private static void closeQuietly(Selector selector) {
        try {
            selector.close();
        } catch (IOException ignored) {
        }
    }

    private final class NioRequestParser {
        private static final int READ_HEAD = 0;
        private static final int READ_BODY = 1;

        private int state = READ_HEAD;
        private RequestHead pendingHead;
        private byte[] pendingBody;
        private int pendingBodyRead;

        ParsedRequest parse(ConnectionState connection) throws Http11ParseException {
            if (state == READ_BODY) {
                return continueBody(connection);
            }
            int headerEnd = findHeaderEnd(connection.input, connection.readPosition, connection.writePosition);
            if (headerEnd < 0) {
                validateIncompleteHead(connection);
                return null;
            }
            RequestHead head = parseHead(connection.input, connection.readPosition, headerEnd);
            int bodyStart = headerEnd + 4;
            if (head.contentLength == 0) {
                connection.readPosition = bodyStart;
                return ParsedRequest.trusted(head.method, head.target, "HTTP/1.1", head.headers, ParsedRequest.emptyBody());
            }
            pendingHead = head;
            pendingBody = new byte[head.contentLength];
            pendingBodyRead = 0;
            int available = Math.min(head.contentLength, connection.writePosition - bodyStart);
            if (available > 0) {
                System.arraycopy(connection.input, bodyStart, pendingBody, 0, available);
                pendingBodyRead = available;
            }
            connection.readPosition = bodyStart + available;
            if (pendingBodyRead == head.contentLength) {
                return completeBody();
            }
            state = READ_BODY;
            return null;
        }

        private ParsedRequest continueBody(ConnectionState connection) {
            int available = Math.min(pendingBody.length - pendingBodyRead,
                    connection.writePosition - connection.readPosition);
            if (available > 0) {
                System.arraycopy(connection.input, connection.readPosition, pendingBody, pendingBodyRead, available);
                pendingBodyRead += available;
                connection.readPosition += available;
            }
            if (pendingBodyRead == pendingBody.length) {
                return completeBody();
            }
            return null;
        }

        private ParsedRequest completeBody() {
            RequestHead head = pendingHead;
            byte[] body = pendingBody;
            pendingHead = null;
            pendingBody = null;
            pendingBodyRead = 0;
            state = READ_HEAD;
            return ParsedRequest.trusted(head.method, head.target, "HTTP/1.1", head.headers, body);
        }

        private void validateIncompleteHead(ConnectionState connection) throws Http11ParseException {
            validateNoBareLineFeed(connection.input, connection.readPosition, connection.writePosition);
            int lineEnd = findCrlf(connection.input, connection.readPosition, connection.writePosition);
            if (lineEnd < 0 && connection.writePosition - connection.readPosition > limits.maxRequestLineBytes() + 2) {
                throw badRequest("line too large");
            }
            if (lineEnd >= 0 && lineEnd - connection.readPosition > limits.maxRequestLineBytes()) {
                throw badRequest("line too large");
            }
            if (lineEnd >= 0 && connection.writePosition - (lineEnd + 2) > limits.maxHeaderBytes()) {
                throw new Http11ParseException(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE, "headers too large");
            }
            if (!connection.hasInputSpace()) {
                throw new Http11ParseException(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE, "headers too large");
            }
        }

        private RequestHead parseHead(byte[] input, int start, int headerEnd) throws Http11ParseException {
            int requestLineEnd = findCrlf(input, start, headerEnd + 2);
            if (requestLineEnd < 0) {
                throw badRequest("invalid request line");
            }
            if (requestLineEnd - start > limits.maxRequestLineBytes()) {
                throw badRequest("line too large");
            }
            if (headerEnd - (requestLineEnd + 2) > limits.maxHeaderBytes()) {
                throw new Http11ParseException(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE, "headers too large");
            }
            RequestLine line = parseRequestLine(input, start, requestLineEnd);
            Map<String, String> headers = new LinkedHashMap<>();
            int contentLength = 0;
            boolean sawContentLength = false;
            int headerCount = 0;
            int position = requestLineEnd + 2;
            while (position < headerEnd) {
                int lineEnd = findCrlf(input, position, headerEnd + 2);
                if (lineEnd < 0) {
                    throw badRequest("malformed header");
                }
                if (lineEnd == position) {
                    break;
                }
                headerCount++;
                if (headerCount > limits.maxHeaderCount()) {
                    throw new Http11ParseException(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE, "too many headers");
                }
                Header header = parseHeader(input, position, lineEnd);
                if (HttpHeaders.equalsName(header.name, HttpHeaders.TRANSFER_ENCODING)) {
                    throw badRequest("Transfer-Encoding is not supported");
                }
                if (HttpHeaders.equalsName(header.name, HttpHeaders.CONTENT_LENGTH)) {
                    int parsedLength = parseContentLength(header.value);
                    if (sawContentLength && parsedLength != contentLength) {
                        throw badRequest("conflicting Content-Length");
                    }
                    contentLength = parsedLength;
                    sawContentLength = true;
                }
                String existingName = existingHeaderName(headers, header.name);
                if (existingName == null) {
                    headers.put(header.name, header.value);
                } else if (!HttpHeaders.equalsName(header.name, HttpHeaders.CONTENT_LENGTH)) {
                    headers.put(existingName, headers.get(existingName) + ", " + header.value);
                }
                position = lineEnd + 2;
            }
            return new RequestHead(line.method, line.target, headers, contentLength);
        }

        private RequestLine parseRequestLine(byte[] input, int start, int end) throws Http11ParseException {
            int firstSpace = indexOf(input, start, end, (byte) ' ');
            int secondSpace = firstSpace < 0 ? -1 : indexOf(input, firstSpace + 1, end, (byte) ' ');
            if (firstSpace <= start || secondSpace <= firstSpace + 1 || secondSpace >= end - 1) {
                throw badRequest("invalid request line");
            }
            if (indexOf(input, secondSpace + 1, end, (byte) ' ') >= 0) {
                throw badRequest("invalid request line");
            }
            if (input[firstSpace + 1] != '/') {
                throw badRequest("request target must be origin-form");
            }
            if (!matchesAscii(input, secondSpace + 1, end, "HTTP/1.1")) {
                throw badRequest("unsupported HTTP version");
            }
            HttpMethod method = parseMethod(input, start, firstSpace);
            String target = new String(input, firstSpace + 1, secondSpace - firstSpace - 1, StandardCharsets.US_ASCII);
            return new RequestLine(method, target);
        }

        private Header parseHeader(byte[] input, int start, int end) throws Http11ParseException {
            if (input[start] == ' ' || input[start] == '\t') {
                throw badRequest("folded headers are not supported");
            }
            int colon = indexOf(input, start, end, (byte) ':');
            if (colon <= start) {
                throw badRequest("malformed header");
            }
            for (int i = start; i < colon; i++) {
                if (!isHeaderNameByte(input[i])) {
                    throw badRequest("invalid header name");
                }
            }
            int valueStart = colon + 1;
            int valueEnd = end;
            while (valueStart < valueEnd && isOptionalWhitespace(input[valueStart])) {
                valueStart++;
            }
            while (valueEnd > valueStart && isOptionalWhitespace(input[valueEnd - 1])) {
                valueEnd--;
            }
            String name = new String(input, start, colon - start, StandardCharsets.US_ASCII);
            String value = new String(input, valueStart, valueEnd - valueStart, StandardCharsets.US_ASCII);
            return new Header(name, value);
        }

        private int parseContentLength(String value) throws Http11ParseException {
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
    }

    private static HttpMethod parseMethod(byte[] input, int start, int end) throws Http11ParseException {
        int length = end - start;
        if (length == 3 && input[start] == 'G' && input[start + 1] == 'E' && input[start + 2] == 'T') {
            return HttpMethod.GET;
        }
        if (length == 4 && input[start] == 'P' && input[start + 1] == 'O'
                && input[start + 2] == 'S' && input[start + 3] == 'T') {
            return HttpMethod.POST;
        }
        if (length == 3 && input[start] == 'P' && input[start + 1] == 'U' && input[start + 2] == 'T') {
            return HttpMethod.PUT;
        }
        if (length == 5 && input[start] == 'P' && input[start + 1] == 'A'
                && input[start + 2] == 'T' && input[start + 3] == 'C' && input[start + 4] == 'H') {
            return HttpMethod.PATCH;
        }
        if (length == 6 && input[start] == 'D' && input[start + 1] == 'E'
                && input[start + 2] == 'L' && input[start + 3] == 'E'
                && input[start + 4] == 'T' && input[start + 5] == 'E') {
            return HttpMethod.DELETE;
        }
        if (length == 7 && input[start] == 'O' && input[start + 1] == 'P'
                && input[start + 2] == 'T' && input[start + 3] == 'I'
                && input[start + 4] == 'O' && input[start + 5] == 'N' && input[start + 6] == 'S') {
            return HttpMethod.OPTIONS;
        }
        if (length == 4 && input[start] == 'H' && input[start + 1] == 'E'
                && input[start + 2] == 'A' && input[start + 3] == 'D') {
            return HttpMethod.HEAD;
        }
        throw badRequest("unsupported HTTP method");
    }

    private static int findHeaderEnd(byte[] input, int start, int end) {
        for (int i = start; i <= end - 4; i++) {
            if (input[i] == '\r'
                    && input[i + 1] == '\n'
                    && input[i + 2] == '\r'
                    && input[i + 3] == '\n') {
                return i;
            }
        }
        return -1;
    }

    private static int findCrlf(byte[] input, int start, int end) {
        for (int i = start; i < end - 1; i++) {
            if (input[i] == '\r' && input[i + 1] == '\n') {
                return i;
            }
            if (input[i] == '\n') {
                return -1;
            }
        }
        return -1;
    }

    private static void validateNoBareLineFeed(byte[] input, int start, int end) throws Http11ParseException {
        for (int i = start; i < end; i++) {
            if (input[i] == '\n' && (i == start || input[i - 1] != '\r')) {
                throw badRequest("invalid CRLF");
            }
            if (input[i] == '\r' && i + 1 < end && input[i + 1] != '\n') {
                throw badRequest("invalid CRLF");
            }
        }
    }

    private static int indexOf(byte[] input, int start, int end, byte value) {
        for (int i = start; i < end; i++) {
            if (input[i] == value) {
                return i;
            }
        }
        return -1;
    }

    private static boolean matchesAscii(byte[] input, int start, int end, String expected) {
        if (end - start != expected.length()) {
            return false;
        }
        for (int i = 0; i < expected.length(); i++) {
            if (input[start + i] != expected.charAt(i)) {
                return false;
            }
        }
        return true;
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

    private record RequestLine(HttpMethod method, String target) {
    }

    private record Header(String name, String value) {
    }

    private record RequestHead(
            HttpMethod method,
            String target,
            Map<String, String> headers,
            int contentLength
    ) {
    }

    private record Target(String path, String query) {
    }

    private final class ConnectionState {
        private final SocketChannel channel;
        private final ConnectionLimiter limiter;
        private final Set<ConnectionState> connections;
        private final byte[] input;
        private final ByteBuffer readView;
        private final NioRequestParser parser = new NioRequestParser();
        private final ArrayDeque<ByteBuffer> pendingWrites = new ArrayDeque<>();

        private SelectionKey key;
        private int readPosition;
        private int writePosition;
        private long pendingWriteBytes;
        private int handledRequests;
        private long lastActivityMillis = System.currentTimeMillis();
        private boolean closeAfterWrite;
        private boolean handlerInProgress;
        private boolean closed;

        private ConnectionState(
                SocketChannel channel,
                ConnectionLimiter limiter,
                Set<ConnectionState> connections,
                int inputCapacity
        ) {
            this.channel = channel;
            this.limiter = limiter;
            this.connections = connections;
            this.input = new byte[inputCapacity];
            this.readView = ByteBuffer.wrap(input);
        }

        private boolean hasInputSpace() {
            return writePosition < input.length;
        }

        private void compactIfNeeded() {
            if (readPosition == 0) {
                return;
            }
            if (readPosition == writePosition) {
                readPosition = 0;
                writePosition = 0;
                return;
            }
            int remaining = writePosition - readPosition;
            System.arraycopy(input, readPosition, input, 0, remaining);
            readPosition = 0;
            writePosition = remaining;
        }
    }

    private final class EngineHandle implements ServerHandle {
        private final ServerSocketChannel serverChannel;
        private final Selector selector;
        private final ExecutorStrategy executorStrategy;
        private final AtomicBoolean running;
        private final Set<ConnectionState> connections;
        private final ConcurrentLinkedQueue<Runnable> selectorTasks;
        private final Thread loopThread;

        private EngineHandle(
                ServerSocketChannel serverChannel,
                Selector selector,
                ExecutorStrategy executorStrategy,
                AtomicBoolean running,
                Set<ConnectionState> connections,
                ConcurrentLinkedQueue<Runnable> selectorTasks,
                Thread loopThread
        ) {
            this.serverChannel = serverChannel;
            this.selector = selector;
            this.executorStrategy = executorStrategy;
            this.running = running;
            this.connections = connections;
            this.selectorTasks = selectorTasks;
            this.loopThread = loopThread;
        }

        @Override
        public int port() {
            return serverChannel.socket().getLocalPort();
        }

        @Override
        public boolean running() {
            return running.get();
        }

        @Override
        public void await() throws InterruptedException {
            loopThread.join();
        }

        @Override
        public void stop() {
            if (running.compareAndSet(true, false)) {
                selectorTasks.add(() -> {
                    for (ConnectionState state : connections) {
                        Http11ServerEngine.close(state);
                    }
                });
                selector.wakeup();
                closeQuietly(serverChannel);
                executorStrategy.close();
            }
        }
    }
}
