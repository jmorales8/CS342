import java.util.ArrayList;

public class GameLogic {
    
    public static boolean isDealerQualified(ArrayList<Card> dealerHand) {
        // Dealer must have at least Queen high to qualify
        int highestRank = 0;
        for (Card card : dealerHand) {
            if (card.getValue() > highestRank) {
                highestRank = card.getValue();
            }
        }
        return highestRank >= 12; // 12 represents Queen
    }
    
    public static String evaluateHand(ArrayList<Card> hand) {
        if (isStraightFlush(hand)) return "Straight Flush";
        if (isThreeOfAKind(hand)) return "Three of a Kind";
        if (isStraight(hand)) return "Straight";
        if (isFlush(hand)) return "Flush";
        if (isPair(hand)) return "Pair";
        return "High Card";
    }
    
    public static int calculatePairPlusWinnings(ArrayList<Card> hand, int bet) {
        String handType = evaluateHand(hand);
        switch (handType) {
            case "Straight Flush": return bet * 40;
            case "Three of a Kind": return bet * 30;
            case "Straight": return bet * 6;
            case "Flush": return bet * 3;
            case "Pair": return bet * 1;
            default: return 0;
        }
    }
    
    public static boolean compareHands(ArrayList<Card> playerHand, ArrayList<Card> dealerHand) {
        String playerHandType = evaluateHand(playerHand);
        String dealerHandType = evaluateHand(dealerHand);
        
        // Compare hand rankings
        int playerRank = getHandRank(playerHandType);
        int dealerRank = getHandRank(dealerHandType);
        
        if (playerRank != dealerRank) {
            return playerRank > dealerRank;
        }
        
        // If hands are the same type, compare high cards
        return compareHighCards(playerHand, dealerHand);
    }
    
    private static boolean isStraightFlush(ArrayList<Card> hand) {
        return isFlush(hand) && isStraight(hand);
    }
    
    private static boolean isThreeOfAKind(ArrayList<Card> hand) {
        if (hand.size() != 3) return false;
        return hand.get(0).getValue() == hand.get(1).getValue() 
            && hand.get(1).getValue() == hand.get(2).getValue();
    }
    
    private static boolean isStraight(ArrayList<Card> hand) {
        if (hand.size() != 3) return false;
        ArrayList<Card> sortedHand = new ArrayList<>(hand);
        sortedHand.sort((a, b) -> a.getValue() - b.getValue());
        
        return sortedHand.get(2).getValue() - sortedHand.get(1).getValue() == 1
            && sortedHand.get(1).getValue() - sortedHand.get(0).getValue() == 1;
    }
    
    private static boolean isFlush(ArrayList<Card> hand) {
        if (hand.size() != 3) return false;
        return hand.get(0).getSuit() == hand.get(1).getSuit() 
            && hand.get(1).getSuit() == hand.get(2).getSuit();
    }
    
    private static boolean isPair(ArrayList<Card> hand) {
        if (hand.size() != 3) return false;
        return hand.get(0).getValue() == hand.get(1).getValue()
            || hand.get(1).getValue() == hand.get(2).getValue()
            || hand.get(0).getValue() == hand.get(2).getValue();
    }
    
    private static int getHandRank(String handType) {
        switch (handType) {
            case "Straight Flush": return 5;
            case "Three of a Kind": return 4;
            case "Straight": return 3;
            case "Flush": return 2;
            case "Pair": return 1;
            default: return 0;
        }
    }
    
    private static boolean compareHighCards(ArrayList<Card> hand1, ArrayList<Card> hand2) {
        ArrayList<Card> sortedHand1 = new ArrayList<>(hand1);
        ArrayList<Card> sortedHand2 = new ArrayList<>(hand2);
        
        sortedHand1.sort((a, b) -> b.getValue() - a.getValue());
        sortedHand2.sort((a, b) -> b.getValue() - a.getValue());
        
        for (int i = 0; i < 3; i++) {
            if (sortedHand1.get(i).getValue() != sortedHand2.get(i).getValue()) {
                return sortedHand1.get(i).getValue() > sortedHand2.get(i).getValue();
            }
        }
        return false; // Completely equal hands
    }
}