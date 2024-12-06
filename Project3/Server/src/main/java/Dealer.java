
import java.util.ArrayList;

public class Dealer {

    private Deck theDeck;
    private ArrayList<Card> dealersHand;

    public Dealer() {
        theDeck = new Deck();
        dealersHand = new ArrayList<>();
    }

    public ArrayList<Card> dealHand() {
        // Check if deck needs reshuffling
        if (theDeck.size() <= 34) {
            theDeck.newDeck();
        }

        ArrayList<Card> hand = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            hand.add(theDeck.remove(0));
        }
        return hand;
    }
}
