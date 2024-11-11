import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class DealerTest {

    @Test
    void testDealerInitialization() {
        Dealer dealer = new Dealer();
        assertNotNull(dealer, "Dealer should be initialized");
    }

    @Test
    void testDealHandSize() {
        Dealer dealer = new Dealer();
        ArrayList<Card> hand = dealer.dealHand();
        assertEquals(3, hand.size(), "Dealt hand should contain exactly 3 cards");
    }

    @Test
    void testDealMultipleHands() {
        Dealer dealer = new Dealer();
        ArrayList<Card> firstHand = dealer.dealHand();
        ArrayList<Card> secondHand = dealer.dealHand();
        
        assertEquals(3, firstHand.size(), "First hand should have 3 cards");
        assertEquals(3, secondHand.size(), "Second hand should have 3 cards");
        assertNotEquals(firstHand, secondHand, "Consecutive hands should be different");
    }

    @Test
    void testDeckReshuffleThreshold() {
        Dealer dealer = new Dealer();
        // Deal enough hands to get to 34 cards
        // 52 - (6 hands * 3 cards) = 34 cards left
        for (int i = 0; i < 6; i++) {
            dealer.dealHand();
        }
        // At this point, we should have 34 cards left
        // Next deal should trigger reshuffle
        ArrayList<Card> hand = dealer.dealHand();
        assertEquals(3, hand.size(), "Hand should be dealt after reshuffle");
    }

    @Test
    void testCardsAreValid() {
        Dealer dealer = new Dealer();
        ArrayList<Card> hand = dealer.dealHand();
        
        for (Card card : hand) {
            assertTrue(card.getValue() >= 2 && card.getValue() <= 14, 
                "Card value should be between 2 and 14");
            assertTrue("CDSH".indexOf(card.getSuit()) != -1, 
                "Card suit should be one of C, D, S, H");
        }
    }

    @Test
    void testDealEntireDeck() {
        Dealer dealer = new Dealer();
        Set<String> uniqueCards = new HashSet<>();
        
        // Deal 6 hands (18 cards)
        for (int i = 0; i < 6; i++) {
            ArrayList<Card> hand = dealer.dealHand();
            for (Card card : hand) {
                uniqueCards.add(card.getSuit() + String.valueOf(card.getValue()));
            }
        }
        
        assertEquals(18, uniqueCards.size(), "Should have dealt 18 unique cards");
    }

    @Test
    void testConsecutiveHandsUnique() {
        Dealer dealer = new Dealer();
        Set<String> allDealtCards = new HashSet<>();
        
        // Deal 5 hands (15 cards)
        for (int i = 0; i < 5; i++) {
            ArrayList<Card> hand = dealer.dealHand();
            for (Card card : hand) {
                String cardString = card.getSuit() + String.valueOf(card.getValue());
                assertTrue(allDealtCards.add(cardString), 
                    "Card " + cardString + " should not be dealt twice");
            }
        }
    }

    @Test
    void testDealAfterReshuffle() {
        Dealer dealer = new Dealer();
        
        // Deal 6 hands to get to reshuffle point
        for (int i = 0; i < 6; i++) {
            dealer.dealHand();
        }
        
        // This should trigger reshuffle
        ArrayList<Card> hand = dealer.dealHand();
        assertNotNull(hand, "Should get a hand after reshuffle");
        assertEquals(3, hand.size(), "Should get 3 cards after reshuffle");
        
        // Verify cards are valid
        for (Card card : hand) {
            assertTrue(card.getValue() >= 2 && card.getValue() <= 14,
                "Cards should have valid values");
            assertTrue("CDSH".indexOf(card.getSuit()) != -1,
                "Cards should have valid suits");
        }
    }

    @Test
    void testNoNullCards() {
        Dealer dealer = new Dealer();
        for (int i = 0; i < 6; i++) {
            ArrayList<Card> hand = dealer.dealHand();
            for (Card card : hand) {
                assertNotNull(card, "Dealt cards should never be null");
            }
        }
    }

    @Test
    void testDealerHandConsistency() {
        Dealer dealer = new Dealer();
        Set<String> uniqueCards = new HashSet<>();
        
        // Deal 6 hands (18 cards)
        for (int i = 0; i < 6; i++) {
            ArrayList<Card> hand = dealer.dealHand();
            for (Card card : hand) {
                String cardString = card.getSuit() + String.valueOf(card.getValue());
                assertTrue(uniqueCards.add(cardString),
                    "Card " + cardString + " should not be dealt twice");
            }
        }
        assertEquals(18, uniqueCards.size(), "Should have 18 unique cards");
    }
}