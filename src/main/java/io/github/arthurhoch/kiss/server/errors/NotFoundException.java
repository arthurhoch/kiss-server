package io.github.arthurhoch.kiss.server.errors;

public class NotFoundException extends KissServerException {
    public NotFoundException(String message) {
        super(message);
    }
}
