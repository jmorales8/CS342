
import java.util.ArrayList;
import java.util.Collections;

public class ThreeCardLogic {

    // Hand rankings from highest to lowest
    private static final int STRAIGHT_FLUSH = 1;
    private static final int THREE_OF_A_KIND = 2;
    private static final int STRAIGHT = 3;
    private static final int FLUSH = 4;
    private static final int PAIR = 5;
    private static final int HIGH_CARD = 0;

    public static int evalHand(ArrayList<Card> hand) {
        if (hand.size() != 3) {
            return HIGH_CARD;
        }

        // Check from highest ranking to lowest
        if (isStraightFlush(hand)) {
            return STRAIGHT_FLUSH;
        }
        if (isThreeOfAKind(hand)) {
            return THREE_OF_A_KIND;  // This should be checked before Straight

                }if (isStraight(hand)) {
            return STRAIGHT;
        }
        if (isFlush(hand)) {
            return FLUSH;
        }
        if (isPair(hand)) {
            return PAIR;
        }
        return HIGH_CARD;
    }

    private static boolean isThreeOfAKind(ArrayList<Card> hand) {
        ArrayList<Integer> values = new ArrayList<>();
        for (Card card : hand) {
            values.add(card.getValue());
        }

        // Count occurrences of each value
        int firstValue = values.get(0);
        int secondValue = values.get(1);

        // If all three values are the same
        if (firstValue == secondValue && secondValue == values.get(2)) {
            return true;
        }
        return false;
    }

    private static boolean isPair(ArrayList<Card> hand) {
        if (isThreeOfAKind(hand)) {
            return false;  // Don't count three of a kind as a pair
        }
        ArrayList<Integer> values = new ArrayList<>();
        for (Card card : hand) {
            values.add(card.getValue());
        }

        // Check each possible pair combination
        return values.get(0).equals(values.get(1))
                || values.get(1).equals(values.get(2))
                || values.get(0).equals(values.get(2));
    }

    private static boolean isStraightFlush(ArrayList<Card> hand) {
        return isFlush(hand) && isStraight(hand);
    }

    private static boolean isFlush(ArrayList<Card> hand) {
        char suit = hand.get(0).getSuit();
        for (Card card : hand) {
            if (card.getSuit() != suit) {
                return false;
            }
        }
        return true;
    }

    private static boolean isStraight(ArrayList<Card> hand) {
        ArrayList<Integer> values = new ArrayList<>();
        for (Card card : hand) {
            values.add(card.getValue());
        }
        Collections.sort(values);

        // Special case: Ace-2-3
        if (values.get(0) == 2 && values.get(1) == 3 && values.get(2) == 14) {
            return true;
        }

        // Normal case: three consecutive values
        return values.get(1) == values.get(0) + 1
                && values.get(2) == values.get(1) + 1;
    }

    public static int compareHands(ArrayList<Card> dealer, ArrayList<Card> player) {
        int dealerRank = evalHand(dealer);
        int playerRank = evalHand(player);

        // First check if both are high card hands
        if (dealerRank == HIGH_CARD && playerRank == HIGH_CARD) {
            return compareHighCards(dealer, player);
        }

        // Otherwise, compare hand rankings
        // Note: Lower number means better hand
        if (dealerRank != HIGH_CARD && playerRank == HIGH_CARD) {
            return 1; // Dealer wins because they have a ranked hand vs high card
        }
        if (dealerRank == HIGH_CARD && playerRank != HIGH_CARD) {
            return 2; // Player wins because they have a ranked hand vs high card
        }

        // If both have ranked hands, lower number wins
        if (dealerRank < playerRank) {
            return 1; // Dealer wins
        }
        if (dealerRank > playerRank) {
            return 2; // Player wins
        }

        // If they have the same type of hand, compare high cards
        return compareHighCards(dealer, player);
    }

    private static int compareHighCards(ArrayList<Card> dealer, ArrayList<Card> player) {
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
        for (int i = 0; i < dealerValues.size(); i++) {
            if (dealerValues.get(i) > playerValues.get(i)) {
                return 1;
            }
            if (dealerValues.get(i) < playerValues.get(i)) {
                return 2;
            }
        }

        return 0; // It's a tie
    }

    private static boolean dealerQualifies(ArrayList<Card> hand) {
        ArrayList<Integer> values = new ArrayList<>();
        for (Card card : hand) {
            values.add(card.getValue());
        }
        Collections.sort(values, Collections.reverseOrder());
        return values.get(0) >= 12; // Queen or better
    }

    public static String determineWinner(ArrayList<Card> dealerHand, ArrayList<Card> playerHand, int anteBet, int playBet) {
        StringBuilder result = new StringBuilder();

        // Add hand descriptions
        result.append("Your hand: ").append(describeHand(playerHand)).append("\n");
        result.append("Dealer's hand: ").append(describeHand(dealerHand)).append("\n");

        // First check if dealer qualifies (Queen high or better)
        if (!dealerQualifies(dealerHand)) {
            result.append("Dealer does not qualify - Queen high or better required.\n");
            result.append("Play bet is returned and Ante pushes to next hand.\n");
            return result.toString();
        }

        int comparison = compareHands(dealerHand, playerHand);
        if (comparison == 1) {
            result.append("Dealer wins! ");
            result.append(getHandTypeName(evalHand(dealerHand))).append(" beats ");
            result.append(getHandTypeName(evalHand(playerHand))).append("\n");
            result.append("You lose $").append(anteBet + playBet);
        } else if (comparison == 2) {
            result.append("You win! ");
            result.append(getHandTypeName(evalHand(playerHand))).append(" beats ");
            result.append(getHandTypeName(evalHand(dealerHand))).append("\n");
            int winnings = (anteBet + playBet) * 2;
            result.append("You win $").append(winnings);
        } else {
            result.append("It's a tie!\nBets are returned");
        }

        return result.toString();
    }

    public static int evalPPWinnings(ArrayList<Card> hand, int bet) {
        if (bet == 0) {
            return 0;
        }

        int handType = evalHand(hand);
        if (handType == STRAIGHT_FLUSH) {
            return bet * 40; 
        }else if (handType == THREE_OF_A_KIND) {
            return bet * 30; 
        }else if (handType == STRAIGHT) {
            return bet * 6; 
        }else if (handType == FLUSH) {
            return bet * 3; 
        }else if (handType == PAIR) {
            return bet; 
        }else {
            return 0;
        }
    }

    private static String getHandTypeName(int handType) {
        if (handType == STRAIGHT_FLUSH) {
            return "Straight Flush"; 
        }else if (handType == THREE_OF_A_KIND) {
            return "Three of a Kind"; 
        }else if (handType == STRAIGHT) {
            return "Straight"; 
        }else if (handType == FLUSH) {
            return "Flush"; 
        }else if (handType == PAIR) {
            return "Pair"; 
        }else {
            return "High Card";
        }
    }

    private static String describeHand(ArrayList<Card> hand) {
        StringBuilder desc = new StringBuilder();
        ArrayList<Card> sortedHand = new ArrayList<>(hand);
        sortedHand.sort((a, b) -> b.getValue() - a.getValue());

        for (int i = 0; i < sortedHand.size(); i++) {
            if (i > 0) {
                desc.append(", ");
            }
            desc.append(getCardName(sortedHand.get(i)));
        }

        desc.append(" (").append(getHandTypeName(evalHand(hand))).append(")");
        return desc.toString();
    }

    private static String getCardName(Card card) {
        String value;
        if (card.getValue() == 14) {
            value = "Ace"; 
        }else if (card.getValue() == 13) {
            value = "King"; 
        }else if (card.getValue() == 12) {
            value = "Queen"; 
        }else if (card.getValue() == 11) {
            value = "Jack"; 
        }else {
            value = String.valueOf(card.getValue());
        }

        String suit;
        if (card.getSuit() == 'H') {
            suit = "♥"; 
        }else if (card.getSuit() == 'D') {
            suit = "♦"; 
        }else if (card.getSuit() == 'C') {
            suit = "♣"; 
        }else if (card.getSuit() == 'S') {
            suit = "♠"; 
        }else {
            suit = String.valueOf(card.getSuit());
        }

        return value + suit;
    }
}
