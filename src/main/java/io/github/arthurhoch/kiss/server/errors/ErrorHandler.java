package io.github.arthurhoch.kiss.server.errors;

import io.github.arthurhoch.kiss.server.http.Response;

@FunctionalInterface
public interface ErrorHandler {
    Response handle(Throwable error);
}
