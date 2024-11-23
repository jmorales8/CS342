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
                // We'll add game logic here later
                Thread.sleep(100); // Temporary, to prevent busy waiting
            }
        } catch (IOException | InterruptedException e) {
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
            e.printStackTrace();
        }
        server.removeClient(this);
    }
}