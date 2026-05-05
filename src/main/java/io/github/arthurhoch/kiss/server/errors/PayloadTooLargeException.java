package io.github.arthurhoch.kiss.server.errors;

public class PayloadTooLargeException extends BadRequestException {
    public PayloadTooLargeException(String message) {
        super(message);
    }
}
