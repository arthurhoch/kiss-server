package io.github.arthurhoch.kiss.server.errors;

public class HeaderTooLargeException extends BadRequestException {
    public HeaderTooLargeException(String message) {
        super(message);
    }
}
