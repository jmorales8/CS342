import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class DeckTest {

    @Test
    void testDeckInitialization() {
        Deck deck = new Deck();
        assertEquals(52, deck.size(), "New deck should contain 52 cards");
        
        // Verify all cards are valid
        for (Card card : deck) {
            assertNotNull(card, "Card should not be null");
            assertTrue("CDSH".indexOf(card.getSuit()) != -1, 
                "Card suit should be one of C, D, S, H");
            assertTrue(card.getValue() >= 2 && card.getValue() <= 14, 
                "Card value should be between 2 and 14");
        }
    }

    @Test
    void testDeckUniqueness() {
        Deck deck = new Deck();
        Set<String> uniqueCards = new HashSet<>();
        
        for (Card card : deck) {
            String cardString = card.getSuit() + String.valueOf(card.getValue());
            assertTrue(uniqueCards.add(cardString), 
                "Deck should not contain duplicate cards");
        }
        
        assertEquals(52, uniqueCards.size(), 
            "Deck should contain 52 unique cards");
    }

    @Test
    void testNewDeckReset() {
        Deck deck = new Deck();
        // Remove some cards
        deck.remove(0);
        deck.remove(0);
        deck.remove(0);
        
        assertEquals(49, deck.size(), "Deck should have 3 cards removed");
        
        deck.newDeck();
        assertEquals(52, deck.size(), "Deck should be reset to 52 cards");
        
        // Verify all suits and values are present after reset
        int[] valueCounts = new int[15];  // 0-14, ignore 0 and 1
        int[] suitCounts = new int[128];  // ASCII values
        
        for (Card card : deck) {
            valueCounts[card.getValue()]++;
            suitCounts[card.getSuit()]++;
        }
        
        // Check correct number of each value (4 of each)
        for (int i = 2; i <= 14; i++) {
            assertEquals(4, valueCounts[i], 
                "Should be 4 cards of value " + i);
        }
        
        // Check correct number of each suit (13 of each)
        assertEquals(13, suitCounts['C'], "Should be 13 clubs");
        assertEquals(13, suitCounts['D'], "Should be 13 diamonds");
        assertEquals(13, suitCounts['S'], "Should be 13 spades");
        assertEquals(13, suitCounts['H'], "Should be 13 hearts");
    }

    @Test
    void testShuffling() {
        Deck deck1 = new Deck();
        Deck deck2 = new Deck();
        
        // Convert both decks to strings for comparison
        ArrayList<String> cards1 = new ArrayList<>();
        ArrayList<String> cards2 = new ArrayList<>();
        
        for (Card card : deck1) {
            cards1.add(card.getSuit() + String.valueOf(card.getValue()));
        }
        for (Card card : deck2) {
            cards2.add(card.getSuit() + String.valueOf(card.getValue()));
        }
        
        // Decks should be in different orders due to shuffling
        assertNotEquals(cards1, cards2, 
            "Two new decks should be in different orders due to shuffling");
    }

    @Test
    void testRemoveOperation() {
        Deck deck = new Deck();
        Card removedCard = deck.remove(0);
        
        assertNotNull(removedCard, "Removed card should not be null");
        assertEquals(51, deck.size(), "Deck should have one less card");
        
        // Verify removed card is no longer in deck
        for (Card card : deck) {
            assertFalse(
                card.getSuit() == removedCard.getSuit() && 
                card.getValue() == removedCard.getValue(),
                "Removed card should not still be in deck"
            );
        }
    }

    @Test
    void testMultipleShuffles() {
        Deck deck = new Deck();
        ArrayList<String> originalOrder = new ArrayList<>();
        for (Card card : deck) {
            originalOrder.add(card.getSuit() + String.valueOf(card.getValue()));
        }
        
        // Perform multiple new deck operations (which include shuffling)
        deck.newDeck();
        ArrayList<String> secondOrder = new ArrayList<>();
        for (Card card : deck) {
            secondOrder.add(card.getSuit() + String.valueOf(card.getValue()));
        }
        
        deck.newDeck();
        ArrayList<String> thirdOrder = new ArrayList<>();
        for (Card card : deck) {
            thirdOrder.add(card.getSuit() + String.valueOf(card.getValue()));
        }
        
        // All three orders should be different
        assertFalse(
            originalOrder.equals(secondOrder) && secondOrder.equals(thirdOrder),
            "Multiple shuffles should produce different orders"
        );
    }

    @Test
    void testDeckIntegrity() {
        Deck deck = new Deck();
        
        // Remove half the deck
        for (int i = 0; i < 26; i++) {
            deck.remove(0);
        }
        
        deck.newDeck();
        
        // Verify deck integrity after reset
        Set<String> uniqueCards = new HashSet<>();
        for (Card card : deck) {
            String cardString = card.getSuit() + String.valueOf(card.getValue());
            assertTrue(uniqueCards.add(cardString), 
                "Deck should contain only unique cards after reset");
        }
        
        assertEquals(52, uniqueCards.size(), 
            "Deck should contain all 52 cards after reset");
    }

    @Test
    void testSuitDistribution() {
        Deck deck = new Deck();
        int clubs = 0, diamonds = 0, spades = 0, hearts = 0;
        
        for (Card card : deck) {
            switch (card.getSuit()) {
                case 'C': clubs++; break;
                case 'D': diamonds++; break;
                case 'S': spades++; break;
                case 'H': hearts++; break;
            }
        }
        
        assertEquals(13, clubs, "Should be 13 clubs in deck");
        assertEquals(13, diamonds, "Should be 13 diamonds in deck");
        assertEquals(13, spades, "Should be 13 spades in deck");
        assertEquals(13, hearts, "Should be 13 hearts in deck");
    }

    @Test
    void testValueDistribution() {
        Deck deck = new Deck();
        int[] valueCounts = new int[15];  // 0-14, ignore 0 and 1
        
        for (Card card : deck) {
            valueCounts[card.getValue()]++;
        }
        
        // Should be exactly 4 of each value (2-14)
        for (int i = 2; i <= 14; i++) {
            assertEquals(4, valueCounts[i], 
                "Should be exactly 4 cards of value " + i);
        }
    }

    @Test
    void testConsecutiveNewDecks() {
        Deck deck = new Deck();
        int initialSize = deck.size();
        
        for (int i = 0; i < 5; i++) {
            deck.newDeck();
            assertEquals(initialSize, deck.size(), 
                "Deck size should remain consistent after newDeck()");
            
            // Verify no null cards
            for (Card card : deck) {
                assertNotNull(card, 
                    "No cards should be null after newDeck()");
            }
        }
    }
}