
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerLogic {

    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private volatile boolean isRunning;
    private CopyOnWriteArrayList<ClientHandler> clients;
    private JavaFXTemplate gui;  // Changed from ServerGUI to JavaFXTemplate
    private int currentPort;

    public ServerLogic() {
        clients = new CopyOnWriteArrayList<>();
        executorService = Executors.newCachedThreadPool();
    }

    public void setGUI(JavaFXTemplate gui) {  // Changed parameter type
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
                clients.add(clientHandler);
                executorService.submit(clientHandler);

                if (gui != null) {
                    gui.updateLog("New client connected");
                }
            } catch (IOException e) {
                if (isRunning && gui != null) {
                    gui.updateLog("Error accepting client: " + e.getMessage());
                }
            }
        }
    }

    public void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            for (ClientHandler client : clients) {
                client.closeConnection();
            }
            clients.clear();

            if (executorService != null) {
                executorService.shutdown();
            }

            if (gui != null) {
                gui.updateLog("Server stopped");
            }
        } catch (IOException e) {
            if (gui != null) {
                gui.updateLog("Error stopping server: " + e.getMessage());
            }
        }
    }

    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        if (gui != null) {
            gui.updateLog("Client disconnected");
        }
    }

    public void logMessage(String message) {
        if (gui != null) {
            gui.updateLog(message);
        }
    }
}
