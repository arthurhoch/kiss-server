package io.github.arthurhoch.kiss.server.protocol.http11;

public record Http11ParserLimits(
        int maxRequestLineBytes,
        int maxHeaderBytes,
        int maxHeaderCount,
        long maxBodyBytes
) {
    public Http11ParserLimits {
        if (maxRequestLineBytes <= 0) {
            throw new IllegalArgumentException("maxRequestLineBytes must be positive");
        }
        if (maxHeaderBytes <= 0) {
            throw new IllegalArgumentException("maxHeaderBytes must be positive");
        }
        if (maxHeaderCount <= 0) {
            throw new IllegalArgumentException("maxHeaderCount must be positive");
        }
        if (maxBodyBytes <= 0) {
            throw new IllegalArgumentException("maxBodyBytes must be positive");
        }
    }
}
