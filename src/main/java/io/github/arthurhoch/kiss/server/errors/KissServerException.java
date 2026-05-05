package io.github.arthurhoch.kiss.server.errors;

public class KissServerException extends RuntimeException {
    public KissServerException(String message) {
        super(message);
    }

    public KissServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
