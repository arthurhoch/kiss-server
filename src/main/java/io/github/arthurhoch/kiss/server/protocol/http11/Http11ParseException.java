package io.github.arthurhoch.kiss.server.protocol.http11;

import io.github.arthurhoch.kiss.server.http.HttpStatus;

public class Http11ParseException extends Exception {
    private final HttpStatus status;

    public Http11ParseException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
