import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ServerLogic server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private volatile boolean isRunning;

    public ClientHandler(Socket socket, ServerLogic server) {
        this.clientSocket = socket;
        this.server = server;
        this.isRunning = true;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());

            while (isRunning) {
                try {
                    Object message = in.readObject();
                    if (message != null) {
                        // Log received message
                        server.logMessage("Received from client: " + message.toString());
                    }
                } catch (EOFException e) {
                    // Client disconnected
                    break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            server.logMessage("Error in client handler: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    public void closeConnection() {
        isRunning = false;
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            server.logMessage("Error closing client connection: " + e.getMessage());
        }
        server.removeClient(this);
    }

    public void sendMessage(String message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            server.logMessage("Error sending message to client: " + e.getMessage());
        }
    }
}