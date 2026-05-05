package io.github.arthurhoch.kiss.server.errors;

public class BadRequestException extends KissServerException {
    public BadRequestException(String message) {
        super(message);
    }
}
