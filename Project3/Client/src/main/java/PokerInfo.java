import java.io.Serializable;
import java.util.ArrayList;

public class PokerInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum MessageType {
        PLACE_BETS,       // Client -> Server: Player places ante and pair plus bets
        DEAL_CARDS,       // Server -> Client: Server deals initial cards
        PLAY_DECISION,    // Client -> Server: Player decides to play or fold
        DEALER_CARDS,     // Server -> Client: Reveals dealer's cards and game result
        GAME_RESULT,      // Server -> Client: Final game result with winnings
        NEW_GAME         // Client -> Server: Request to start new game
    }
    
    // Message type and game state
    private MessageType messageType;
    
    // Betting information
    private int anteBet;
    private int pairPlusBet;
    private int playBet;
    private boolean playerFolded;
    
    // Card information
    private ArrayList<Card> playerCards;
    private ArrayList<Card> dealerCards;
    
    // Game results
    private boolean dealerQualified;
    private boolean playerWon;
    private int pairPlusWinnings;
    private int totalWinnings;
    private int pushedAntes;
    private String resultMessage;
    
    // Constructors for different message types
    public static PokerInfo placeBets(int anteBet, int pairPlusBet) {
        PokerInfo info = new PokerInfo();
        info.messageType = MessageType.PLACE_BETS;
        info.anteBet = anteBet;
        info.pairPlusBet = pairPlusBet;
        return info;
    }
    
    public static PokerInfo playDecision(boolean fold, int anteBet) {
        PokerInfo info = new PokerInfo();
        info.messageType = MessageType.PLAY_DECISION;
        info.playerFolded = fold;
        info.playBet = anteBet;
        info.anteBet = anteBet;
        return info;
    }
    
    public static PokerInfo newGame() {
        PokerInfo info = new PokerInfo();
        info.messageType = MessageType.NEW_GAME;
        return info;
    }
    
    // Getters and setters
    public MessageType getMessageType() { return messageType; }
    public void setMessageType(MessageType type) { this.messageType = type; }
    
    public int getAnteBet() { return anteBet; }
    public void setAnteBet(int bet) { this.anteBet = bet; }
    
    public int getPairPlusBet() { return pairPlusBet; }
    public void setPairPlusBet(int bet) { this.pairPlusBet = bet; }
    
    public int getPlayBet() { return playBet; }
    public void setPlayBet(int bet) { this.playBet = bet; }
    
    public boolean isPlayerFolded() { return playerFolded; }
    public void setPlayerFolded(boolean folded) { this.playerFolded = folded; }
    
    public ArrayList<Card> getPlayerCards() { return playerCards; }
    public void setPlayerCards(ArrayList<Card> cards) { this.playerCards = cards; }
    
    public ArrayList<Card> getDealerCards() { return dealerCards; }
    public void setDealerCards(ArrayList<Card> cards) { this.dealerCards = cards; }
    
    public boolean isDealerQualified() { return dealerQualified; }
    public void setDealerQualified(boolean qualified) { this.dealerQualified = qualified; }
    
    public boolean isPlayerWon() { return playerWon; }
    public void setPlayerWon(boolean won) { this.playerWon = won; }
    
    public int getPairPlusWinnings() { return pairPlusWinnings; }
    public void setPairPlusWinnings(int winnings) { this.pairPlusWinnings = winnings; }
    
    public int getTotalWinnings() { return totalWinnings; }
    public void setTotalWinnings(int winnings) { this.totalWinnings = winnings; }
    
    public int getPushedAntes() { return pushedAntes; }
    public void setPushedAntes(int antes) { this.pushedAntes = antes; }
    
    public String getResultMessage() { return resultMessage; }
    public void setResultMessage(String message) { this.resultMessage = message; }
}