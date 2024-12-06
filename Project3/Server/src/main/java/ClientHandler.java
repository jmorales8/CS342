
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private ServerLogic server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private volatile boolean isRunning;

    // Game state
    private Dealer dealer;
    private ArrayList<Card> playerCards;
    private ArrayList<Card> dealerCards;
    private int playerTotalWinnings = 0;
    private int pushedAntes = 0;

    public ClientHandler(Socket socket, ServerLogic server) {
        this.clientSocket = socket;
        this.server = server;
        this.isRunning = true;
        this.dealer = new Dealer();
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
                } catch (EOFException | SocketException e) {
                    // Client disconnected
                    server.logMessage("Client disconnected unexpectedly");
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
        if (!isRunning) {
            return; // Already closed
        }

        isRunning = false;
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                server.logMessage("Client socket closed");
            }
        } catch (IOException e) {
            server.logMessage("Error closing client connection: " + e.getMessage());
        } finally {
            server.removeClient(this);
            server.logMessage("Client handler removed from server");
        }
    }

    void handlePokerInfo(PokerInfo info) {
        try {
            switch (info.getMessageType()) {
                case PLACE_BETS:
                    handleInitialDeal(info);
                    break;

                case PLAY_DECISION:
                    handlePlayDecision(info);
                    break;
            }
        } catch (IOException e) {
            server.logMessage("Error sending response: " + e.getMessage());
        }
    }

    private void handleInitialDeal(PokerInfo info) throws IOException {
        // Deal new hands
        playerCards = dealer.dealHand();
        dealerCards = dealer.dealHand();

        // Create response with cards
        PokerInfo response = new PokerInfo();
        response.setMessageType(PokerInfo.MessageType.DEAL_CARDS);
        response.setPlayerCards(playerCards);
        response.setDealerCards(dealerCards);

        out.writeObject(response);
        out.flush();
        server.logMessage("Dealt cards to client");
    }

    private void handlePlayDecision(PokerInfo info) throws IOException {
        PokerInfo response = new PokerInfo();
        response.setMessageType(PokerInfo.MessageType.DEALER_CARDS);
        response.setDealerCards(dealerCards);

        StringBuilder resultMessage = new StringBuilder();
        int anteBet = info.getAnteBet();

        System.out.println("Starting hand - Ante bet: $" + anteBet);
        System.out.println("Current pushed antes: $" + pushedAntes);

        server.logMessage("==== New Play Decision ====");
        server.logMessage("Ante Bet: $" + info.getAnteBet());
        server.logMessage("Pair Plus Bet: $" + info.getPairPlusBet());
        server.logMessage("Player Folded: " + info.isPlayerFolded());
        server.logMessage("Current Total Winnings: $" + playerTotalWinnings);

        if (!info.isPlayerFolded()) {
            boolean dealerQualified = GameLogic.isDealerQualified(dealerCards);
            System.out.println("Dealer qualified: " + dealerQualified);

            if (!dealerQualified) {
                pushedAntes += anteBet;
                resultMessage.append("Dealer doesn't qualify. Play bet returned. Total pushed antes: $")
                        .append(pushedAntes);
            } else {
                boolean playerWins = GameLogic.compareHands(playerCards, dealerCards);
                System.out.println("Player wins: " + playerWins);

                if (playerWins) {
                    int totalAnte = anteBet + pushedAntes; // Combine current ante with pushed antes
                    int winnings = totalAnte * 2; // Win pays 1:1 on total ante amount
                    playerTotalWinnings += winnings;
                    pushedAntes = 0;
                    resultMessage.append("Player wins! Winnings: $").append(winnings);
                } else {
                    pushedAntes = 0;
                    resultMessage.append("Dealer wins. Player loses ante and play bets.");
                }
            }
        } else {
            pushedAntes = 0; // Reset pushed antes on fold
            resultMessage.append("Player folded. Ante lost.");
        }

        if (info.getPairPlusBet() > 0) {
            int pairPlusWinnings = ThreeCardLogic.evalPPWinnings(playerCards, info.getPairPlusBet());
            if (pairPlusWinnings > 0) {
                playerTotalWinnings += pairPlusWinnings;
                resultMessage.append("\nPair Plus wins: $").append(pairPlusWinnings);
            } else {
                resultMessage.append("\nPair Plus bet lost.");
            }
        }

        server.logMessage("\nSending Response:");
        server.logMessage("Total Winnings being sent: $" + playerTotalWinnings);
        server.logMessage("Pushed Antes being sent: $" + pushedAntes);
        server.logMessage("Result Message: " + resultMessage.toString());
        server.logMessage("================================");

        response.setResultMessage(resultMessage.toString());
        response.setTotalWinnings(playerTotalWinnings);
        response.setPushedAntes(pushedAntes);

        out.writeObject(response);
        out.flush();
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
