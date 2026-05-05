package io.github.arthurhoch.kiss.server.runtime;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.function.Consumer;

public final class ConnectionAcceptor implements Runnable {
    private final ServerSocket serverSocket;
    private final Consumer<Socket> connectionConsumer;

    public ConnectionAcceptor(ServerSocket serverSocket, Consumer<Socket> connectionConsumer) {
        this.serverSocket = Objects.requireNonNull(serverSocket, "serverSocket");
        this.connectionConsumer = Objects.requireNonNull(connectionConsumer, "connectionConsumer");
    }

    @Override
    public void run() {
        while (!serverSocket.isClosed()) {
            try {
                connectionConsumer.accept(serverSocket.accept());
            } catch (IOException e) {
                if (!serverSocket.isClosed()) {
                    throw new IllegalStateException("failed to accept connection", e);
                }
            }
        }
    }
}
