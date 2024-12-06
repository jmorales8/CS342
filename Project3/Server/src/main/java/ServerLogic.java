
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.scene.control.Label;

public class ServerLogic {

    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private volatile boolean isRunning;
    private CopyOnWriteArrayList<ClientHandler> clients;
    private JavaFXTemplate gui;
    private int currentPort;
    private Label clientsLabel;

    public ServerLogic() {
        clients = new CopyOnWriteArrayList<>();
        executorService = Executors.newCachedThreadPool();
    }

    private void updateConnectedUsersCount() {
        int connectedUsers = clients.size();
        if (gui != null) {
            Platform.runLater(() -> {
                if (clientsLabel != null) {
                    clientsLabel.setText("Connected Clients: " + connectedUsers);
                }
                gui.updateLog("Number of Connected Users: " + connectedUsers);
            });
        }
    }

    public void addClient(ClientHandler client) {
        clients.add(client);
        updateConnectedUsersCount();
        logMessage("New client connected. Total connected users: " + clients.size());
    }

    public void removeClient(ClientHandler clientHandler) {
        if (clients.remove(clientHandler)) {
            logMessage("Client removed. Current connected users: " + clients.size());
            updateConnectedUsersCount();
        }
    }

    public void setClientsLabel(Label label) {
        this.clientsLabel = label;
    }

    public void setGUI(JavaFXTemplate gui) {
        this.gui = gui;
    }

    public boolean startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            currentPort = port;
            isRunning = true;

            if (gui != null) {
                gui.updateLog("Server started on port " + port);
            }

            // Start accepting clients on a separate thread
            Thread acceptThread = new Thread(this::acceptClients);
            acceptThread.setDaemon(true);
            acceptThread.start();

            return true;
        } catch (IOException e) {
            if (gui != null) {
                gui.updateLog("Error starting server: " + e.getMessage());
            }
            return false;
        }
    }

    private void acceptClients() {
        while (isRunning) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                addClient(clientHandler);
                executorService.submit(clientHandler);
            } catch (IOException e) {
                if (isRunning) {
                    logMessage("Error accepting client: " + e.getMessage());
                }
            }
        }
    }

    public void stopServer() {
        isRunning = false;
        try {
            // First close the server socket
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                logMessage("Server socket closed");
            }

            // Then close all client connections
            for (ClientHandler client : new ArrayList<>(clients)) {
                client.closeConnection();
            }
            clients.clear();

            if (executorService != null) {
                executorService.shutdown();
                if (executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    logMessage("Server executor service shut down successfully");
                } else {
                    logMessage("Server executor service forced shutdown");
                    executorService.shutdownNow();
                }
            }

            logMessage("Server stopped completely");
        } catch (IOException | InterruptedException e) {
            logMessage("Error stopping server: " + e.getMessage());
        }
    }

    public void logMessage(String message) {
        if (gui != null) {
            Platform.runLater(() -> gui.updateLog(message));
        }
        // Also print to console for debugging
        System.out.println("[Server] " + message);
    }
}
