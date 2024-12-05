
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

    // Game state
    private Dealer dealer;
    private ArrayList<Card> playerCards;
    private ArrayList<Card> dealerCards;
    private int playerTotalWinnings = 0; // Track total winnings across games
    private int pushedAntes = 0; // Track pushed antes

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

    private void logMoneyChange(String action, int amount, int totalBefore, int totalAfter) {
        String message = String.format(
                " Money Change: Action: %s Amount: $%d Total Before: $%d Total After: $%d Current Pushed Antes: $%d ---------------------------------------- ",
                action, amount, totalBefore, totalAfter, pushedAntes);
        server.logMessage(message);
    }

    private void handlePokerInfo(PokerInfo info) {
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
        int playBet = info.getPlayBet();
        
        if (!info.isPlayerFolded()) {
            boolean dealerQualified = GameLogic.isDealerQualified(dealerCards);
            if (!dealerQualified) {
                pushedAntes += anteBet;
                resultMessage.append("Dealer doesn't qualify. Ante pushed to next hand.");
            } else {
                boolean playerWins = GameLogic.compareHands(playerCards, dealerCards);
                if (playerWins) {
                    playerTotalWinnings += (anteBet + playBet) * 2;
                    resultMessage.append("Player wins! Total won: $").append((anteBet + playBet) * 2);
                } else {
                    resultMessage.append("Dealer wins. Player loses ante.");
                }
            }
        } else {
            resultMessage.append("Player folded. Ante lost.");
        }
        
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
