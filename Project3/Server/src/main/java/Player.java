
import java.io.Serializable;
import java.util.ArrayList;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;

    ArrayList<Card> hand;
    int anteBet;
    int playBet;
    int pairPlusBet;
    int totalWinnings;

    public Player() {
        hand = new ArrayList<>();
        anteBet = 0;
        playBet = 0;
        pairPlusBet = 0;
        totalWinnings = 0;
    }
}
