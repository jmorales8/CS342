
import java.util.ArrayList;
import java.util.Collections;

public class ThreeCardLogic {

    public static int evalHand(ArrayList<Card> hand) {
        if (hand.size() != 3) {
            return 0; // Invalid hand
        }
        if (isFlush(hand) && isStraight(hand)) {
            return 1; // Straight flush

        }
        if (isThreeOfAKind(hand)) {
            return 2; // Three of a kind

        }
        if (isStraight(hand)) {
            return 3; // Straight

        }
        if (isFlush(hand)) {
            return 4; // Flush

        }
        if (isPair(hand)) {
            return 5; // Pair

        }
        return 0; // High card
    }

    private static boolean isFlush(ArrayList<Card> hand) {
        // All cards must have the same suit
        char suit = hand.get(0).getSuit();
        for (Card card : hand) {
            if (card.getSuit() != suit) {
                return false;
            }
        }
        return true;
    }

    private static boolean isStraight(ArrayList<Card> hand) {
        // Get the values and sort them
        ArrayList<Integer> values = new ArrayList<>();
        for (Card card : hand) {
            values.add(card.getValue());
        }
        Collections.sort(values);

        // Special case for Ace-2-3
        if (values.get(0) == 2 && values.get(1) == 3 && values.get(2) == 14) {
            return true;
        }

        // Normal case: check if values are consecutive
        return values.get(2) == values.get(1) + 1
                && values.get(1) == values.get(0) + 1;
    }

    private static boolean isThreeOfAKind(ArrayList<Card> hand) {
        // All three cards must have the same value
        return hand.get(0).getValue() == hand.get(1).getValue()
                && hand.get(1).getValue() == hand.get(2).getValue();
    }

    private static boolean isPair(ArrayList<Card> hand) {
        // Check each possible pair combination
        return hand.get(0).getValue() == hand.get(1).getValue()
                || hand.get(1).getValue() == hand.get(2).getValue()
                || hand.get(0).getValue() == hand.get(2).getValue();
    }

    public static int evalPPWinnings(ArrayList<Card> hand, int bet) {
        int handValue = evalHand(hand);
        switch (handValue) {
            case 1:
                return bet * 40; // Straight flush
            case 2:
                return bet * 30; // Three of a kind
            case 3:
                return bet * 6; // Straight
            case 4:
                return bet * 3; // Flush
            case 5:
                return bet; // Pair
            default:
                return 0; // Lost bet
        }
    }

    public static int compareHands(ArrayList<Card> dealer, ArrayList<Card> player) {
        // First compare hand types
        int dealerHandValue = evalHand(dealer);
        int playerHandValue = evalHand(player);

        if (dealerHandValue < playerHandValue) {
            return 2; // Player wins

        }
        if (dealerHandValue > playerHandValue) {
            return 1; // Dealer wins
        }
        // If same hand type, compare high cards
        ArrayList<Integer> dealerValues = new ArrayList<>();
        ArrayList<Integer> playerValues = new ArrayList<>();

        // Get all values
        for (Card card : dealer) {
            dealerValues.add(card.getValue());
        }
        for (Card card : player) {
            playerValues.add(card.getValue());
        }

        // Sort in descending order
        Collections.sort(dealerValues, Collections.reverseOrder());
        Collections.sort(playerValues, Collections.reverseOrder());

        // Compare each card, starting with highest
        for (int i = 0; i < 3; i++) {
            if (dealerValues.get(i) > playerValues.get(i)) {
                return 1;
            }
            if (dealerValues.get(i) < playerValues.get(i)) {
                return 2;
            }
        }

        return 0; // It's a tie
    }
}
