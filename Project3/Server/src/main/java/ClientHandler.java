
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

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
                if (message instanceof PokerInfo) {
                    PokerInfo info = (PokerInfo) message;
                    handlePokerInfo(info);
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


private void handlePokerInfo(PokerInfo info) {
    try {
        switch (info.getMessageType()) {
            case PLACE_BETS:
                // Use Dealer to generate cards
                Dealer dealer = new Dealer();
                ArrayList<Card> playerCards = dealer.dealHand();
                ArrayList<Card> dealerCards = dealer.dealHand();

                // Create response with cards
                PokerInfo response = new PokerInfo();
                response.setMessageType(PokerInfo.MessageType.DEAL_CARDS);
                response.setPlayerCards(playerCards);
                response.setDealerCards(dealerCards);

                // Send to client
                out.writeObject(response);
                out.flush();

                server.logMessage("Dealt cards to client");
                break;

        }
    } catch (IOException e) {
        server.logMessage("Error sending response: " + e.getMessage());
    }
}

public void sendPokerInfo(PokerInfo info) {
    try {
        out.writeObject(info);
        out.flush();
    } catch (IOException e) {
        server.logMessage("Error sending poker info: " + e.getMessage());
    }
}
}
