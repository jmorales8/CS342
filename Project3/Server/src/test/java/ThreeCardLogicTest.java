import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

class ThreeCardLogicTest {
    @Test
    void testEvalHand() {
        // Test Straight Flush
        ArrayList<Card> straightFlush = new ArrayList<>();
        straightFlush.add(new Card('H', 2));
        straightFlush.add(new Card('H', 3));
        straightFlush.add(new Card('H', 4));
        assertEquals(1, ThreeCardLogic.evalHand(straightFlush));

        // Test Three of a Kind
        ArrayList<Card> threeKind = new ArrayList<>();
        threeKind.add(new Card('H', 7));
        threeKind.add(new Card('D', 7));
        threeKind.add(new Card('C', 7));
        assertEquals(2, ThreeCardLogic.evalHand(threeKind));

        // Test High Card
        ArrayList<Card> highCard = new ArrayList<>();
        highCard.add(new Card('H', 2));
        highCard.add(new Card('D', 7));
        highCard.add(new Card('C', 10));
        assertEquals(0, ThreeCardLogic.evalHand(highCard));
    }

    @Test
    void testEvalPPWinnings() {
        // Test Straight Flush payout
        ArrayList<Card> straightFlush = new ArrayList<>();
        straightFlush.add(new Card('H', 2));
        straightFlush.add(new Card('H', 3));
        straightFlush.add(new Card('H', 4));
        assertEquals(200, ThreeCardLogic.evalPPWinnings(straightFlush, 5));

        // Test Pair payout
        ArrayList<Card> pair = new ArrayList<>();
        pair.add(new Card('H', 7));
        pair.add(new Card('D', 7));
        pair.add(new Card('C', 4));
        assertEquals(5, ThreeCardLogic.evalPPWinnings(pair, 5));

        // Test losing hand
        ArrayList<Card> loser = new ArrayList<>();
        loser.add(new Card('H', 2));
        loser.add(new Card('D', 7));
        loser.add(new Card('C', 10));
        assertEquals(0, ThreeCardLogic.evalPPWinnings(loser, 5));
    }

    @Test
    void testCompareHands() {
        // Test dealer wins with higher hand type
        ArrayList<Card> dealerHand = new ArrayList<>();
        dealerHand.add(new Card('H', 7));
        dealerHand.add(new Card('D', 7));
        dealerHand.add(new Card('C', 7));

        ArrayList<Card> playerHand = new ArrayList<>();
        playerHand.add(new Card('H', 2));
        playerHand.add(new Card('H', 3));
        playerHand.add(new Card('H', 4));
        
        assertEquals(1, ThreeCardLogic.compareHands(dealerHand, playerHand));

        // Test player wins with higher card
        ArrayList<Card> highDealer = new ArrayList<>();
        highDealer.add(new Card('H', 2));
        highDealer.add(new Card('D', 5));
        highDealer.add(new Card('C', 7));

        ArrayList<Card> highPlayer = new ArrayList<>();
        highPlayer.add(new Card('S', 3));
        highPlayer.add(new Card('D', 6));
        highPlayer.add(new Card('C', 9));
        
        assertEquals(2, ThreeCardLogic.compareHands(highDealer, highPlayer));
    }
}