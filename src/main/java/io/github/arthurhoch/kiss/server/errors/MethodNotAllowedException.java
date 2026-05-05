package io.github.arthurhoch.kiss.server.errors;

public class MethodNotAllowedException extends KissServerException {
    public MethodNotAllowedException(String message) {
        super(message);
    }
}
