package io.github.arthurhoch.kiss.server.routing;

@FunctionalInterface
public interface DirectHandler {
    byte[] handle() throws Exception;
}
