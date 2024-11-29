
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

// In ClientHandler class, modify the run() method's message handling:
    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());

            while (isRunning) {
                try {
                    Object message = in.readObject();
                    if (message != null) {
                        // Handle ping specifically
                        if (message.equals("PING")) {
                            server.logMessage("Ping received from client");
                            // Send pong back to client
                            sendMessage("PONG");
                        } else {
                            // Handle other messages
                            server.logMessage("Received from client: " + message.toString());
                        }
                    }
                } catch (EOFException e) {
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
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (clientSocket != null) {
                clientSocket.close();
            }
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
