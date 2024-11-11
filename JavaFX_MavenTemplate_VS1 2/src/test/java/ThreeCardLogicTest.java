import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class ThreeCardLogicTest {
    
    private ArrayList<Card> createHand(char suit1, int value1, char suit2, int value2, char suit3, int value3) {
        ArrayList<Card> hand = new ArrayList<>();
        hand.add(new Card(suit1, value1));
        hand.add(new Card(suit2, value2));
        hand.add(new Card(suit3, value3));
        return hand;
    }

    @Test
    void testStraightFlush() {
        ArrayList<Card> hand = createHand('H', 7, 'H', 8, 'H', 9);
        assertEquals(1, ThreeCardLogic.evalHand(hand), "Should be a straight flush");
        
        // Test Ace-2-3 straight flush
        hand = createHand('H', 14, 'H', 2, 'H', 3);
        assertEquals(1, ThreeCardLogic.evalHand(hand), "Should be an Ace-2-3 straight flush");
    }

    @Test
    void testThreeOfAKind() {
        ArrayList<Card> hand = createHand('H', 7, 'D', 7, 'C', 7);
        assertEquals(2, ThreeCardLogic.evalHand(hand), "Should be three of a kind");
        
        // Test with Aces
        hand = createHand('H', 14, 'D', 14, 'C', 14);
        assertEquals(2, ThreeCardLogic.evalHand(hand), "Should be three Aces");
    }

    @Test
    void testStraight() {
        ArrayList<Card> hand = createHand('H', 7, 'D', 8, 'C', 9);
        assertEquals(3, ThreeCardLogic.evalHand(hand), "Should be a straight");
        
        // Test Ace-2-3 straight
        hand = createHand('H', 14, 'D', 2, 'C', 3);
        assertEquals(3, ThreeCardLogic.evalHand(hand), "Should be an Ace-2-3 straight");
    }

    @Test
    void testFlush() {
        ArrayList<Card> hand = createHand('H', 2, 'H', 5, 'H', 9);
        assertEquals(4, ThreeCardLogic.evalHand(hand), "Should be a flush");
    }

    @Test
    void testPair() {
        ArrayList<Card> hand = createHand('H', 7, 'D', 7, 'C', 9);
        assertEquals(5, ThreeCardLogic.evalHand(hand), "Should be a pair");
    }

    @Test
    void testHighCard() {
        ArrayList<Card> hand = createHand('H', 2, 'D', 7, 'C', 10);
        assertEquals(0, ThreeCardLogic.evalHand(hand), "Should be high card");
    }

    @Test
    void testPairPlusPayouts() {
        // Test all payout scenarios
        ArrayList<Card> straightFlush = createHand('H', 7, 'H', 8, 'H', 9);
        assertEquals(400, ThreeCardLogic.evalPPWinnings(straightFlush, 10), "Straight flush should pay 40:1");

        ArrayList<Card> threeOfKind = createHand('H', 7, 'D', 7, 'C', 7);
        assertEquals(300, ThreeCardLogic.evalPPWinnings(threeOfKind, 10), "Three of a kind should pay 30:1");

        ArrayList<Card> straight = createHand('H', 7, 'D', 8, 'C', 9);
        assertEquals(60, ThreeCardLogic.evalPPWinnings(straight, 10), "Straight should pay 6:1");

        ArrayList<Card> flush = createHand('H', 2, 'H', 5, 'H', 9);
        assertEquals(30, ThreeCardLogic.evalPPWinnings(flush, 10), "Flush should pay 3:1");

        ArrayList<Card> pair = createHand('H', 7, 'D', 7, 'C', 9);
        assertEquals(10, ThreeCardLogic.evalPPWinnings(pair, 10), "Pair should pay 1:1");
    }

    @Test
    void testHandComparison() {
        ArrayList<Card> straightFlush = createHand('H', 7, 'H', 8, 'H', 9);
        ArrayList<Card> threeOfKind = createHand('H', 7, 'D', 7, 'C', 7);
        
        assertEquals(1, ThreeCardLogic.compareHands(straightFlush, threeOfKind), 
            "Straight flush should beat three of a kind");
    }

    @Test
    void testEqualHandComparison() {
        ArrayList<Card> pair1 = createHand('H', 7, 'D', 7, 'C', 9);
        ArrayList<Card> pair2 = createHand('S', 7, 'C', 7, 'H', 9);
        
        assertEquals(0, ThreeCardLogic.compareHands(pair1, pair2), 
            "Equal hands should tie");
    }

    @Test
    void testHighCardComparison() {
        ArrayList<Card> highCard1 = createHand('H', 14, 'D', 10, 'C', 8);
        ArrayList<Card> highCard2 = createHand('S', 13, 'C', 10, 'H', 8);
        
        assertEquals(1, ThreeCardLogic.compareHands(highCard1, highCard2), 
            "Ace high should beat King high");
    }

    @Test
    void testDealerQualification() {
        // Dealer with Queen high
        ArrayList<Card> dealerHand = createHand('H', 12, 'D', 10, 'C', 8);
        ArrayList<Card> playerHand = createHand('S', 11, 'C', 10, 'H', 8);
        String result = ThreeCardLogic.determineWinner(dealerHand, playerHand, 10, 10);
        assertFalse(result.contains("Dealer does not qualify"), 
            "Dealer should qualify with Queen high");

        // Dealer with Jack high
        dealerHand = createHand('H', 11, 'D', 10, 'C', 8);
        result = ThreeCardLogic.determineWinner(dealerHand, playerHand, 10, 10);
        assertTrue(result.contains("Dealer does not qualify"), 
            "Dealer should not qualify with Jack high");
    }

    @Test
    void testEmptyHand() {
        ArrayList<Card> emptyHand = new ArrayList<>();
        assertEquals(0, ThreeCardLogic.evalHand(emptyHand), 
            "Empty hand should be evaluated as high card");
    }

    @Test
    void testInvalidHandSize() {
        ArrayList<Card> twoCardHand = createHand('H', 7, 'D', 8, 'C', 9);
        twoCardHand.remove(0);
        assertEquals(0, ThreeCardLogic.evalHand(twoCardHand), 
            "Invalid hand size should be evaluated as high card");
    }

    @Test
    void testAceHighStraight() {
        ArrayList<Card> aceHighStraight = createHand('H', 14, 'D', 13, 'C', 12);
        assertEquals(3, ThreeCardLogic.evalHand(aceHighStraight), 
            "Should be a straight (Ace high)");
    }

    @Test
    void testAllPossibleStraights() {
        // Test every possible straight combination
        ArrayList<Card> straight;
        
        // Ace-2-3
        straight = createHand('H', 14, 'D', 2, 'C', 3);
        assertEquals(3, ThreeCardLogic.evalHand(straight), "A-2-3 should be a straight");
        
        // Regular straights
        for (int i = 2; i <= 12; i++) {
            straight = createHand('H', i, 'D', i+1, 'C', i+2);
            assertEquals(3, ThreeCardLogic.evalHand(straight), 
                i + "-" + (i+1) + "-" + (i+2) + " should be a straight");
        }
    }

    @Test
    void testPairComparisons() {
        // Test pairs with different kickers
        ArrayList<Card> pairWithAce = createHand('H', 7, 'D', 7, 'C', 14);
        ArrayList<Card> pairWithKing = createHand('S', 7, 'C', 7, 'H', 13);
        
        assertEquals(1, ThreeCardLogic.compareHands(pairWithAce, pairWithKing), 
            "Pair with Ace kicker should beat pair with King kicker");
    }

    @Test
    void testFlushComparisons() {
        ArrayList<Card> aceHighFlush = createHand('H', 14, 'H', 10, 'H', 8);
        ArrayList<Card> kingHighFlush = createHand('D', 13, 'D', 10, 'D', 8);
        
        assertEquals(1, ThreeCardLogic.compareHands(aceHighFlush, kingHighFlush), 
            "Ace-high flush should beat King-high flush");
    }

    @Test
    void testPairPlusZeroBet() {
        ArrayList<Card> pair = createHand('H', 7, 'D', 7, 'C', 9);
        assertEquals(0, ThreeCardLogic.evalPPWinnings(pair, 0), 
            "Zero bet should return zero winnings");
    }

    @Test
    void testDealerPlayerTie() {
        ArrayList<Card> hand1 = createHand('H', 14, 'D', 10, 'C', 8);
        ArrayList<Card> hand2 = createHand('S', 14, 'H', 10, 'D', 8);
        
        String result = ThreeCardLogic.determineWinner(hand1, hand2, 10, 10);
        assertTrue(result.contains("tie"), "Should be a tie with identical rankings");
    }

    @Test
    void testComplexHandComparison() {
        // Test multiple scenarios in sequence
        ArrayList<Card> straightFlush = createHand('H', 7, 'H', 8, 'H', 9);
        ArrayList<Card> threeOfKind = createHand('H', 7, 'D', 7, 'C', 7);
        ArrayList<Card> straight = createHand('H', 7, 'D', 8, 'C', 9);
        ArrayList<Card> flush = createHand('H', 2, 'H', 5, 'H', 9);
        ArrayList<Card> pair = createHand('H', 7, 'D', 7, 'C', 9);
        
        assertTrue(ThreeCardLogic.compareHands(straightFlush, threeOfKind) == 1);
        assertTrue(ThreeCardLogic.compareHands(threeOfKind, straight) == 1);
        assertTrue(ThreeCardLogic.compareHands(straight, flush) == 1);
        assertTrue(ThreeCardLogic.compareHands(flush, pair) == 1);
    }
}
