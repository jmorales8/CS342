import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

class ThreeCardPokerTest {

  @Test
  void testEvalHand_StraightFlush() {
    ArrayList<Card> hand = new ArrayList<>();
    hand.add(new Card('H', 2));
    hand.add(new Card('H', 3));
    hand.add(new Card('H', 4));
    assertEquals(1, ThreeCardLogic.evalHand(hand));
  }

  @Test
  void testEvalHand_ThreeOfAKind() {
    ArrayList<Card> hand = new ArrayList<>();
    hand.add(new Card('H', 7));
    hand.add(new Card('D', 7));
    hand.add(new Card('C', 7));
    assertEquals(2, ThreeCardLogic.evalHand(hand));
  }

  @Test
  void testEvalPPWinnings_StraightFlush() {
    ArrayList<Card> hand = new ArrayList<>();
    hand.add(new Card('H', 2));
    hand.add(new Card('H', 3));
    hand.add(new Card('H', 4));
    assertEquals(200, ThreeCardLogic.evalPPWinnings(hand, 5));
  }

  @Test
  void testDealerQualification() {
    ArrayList<Card> qualifyingHand = new ArrayList<>();
    qualifyingHand.add(new Card('H', 12)); // Queen
    qualifyingHand.add(new Card('D', 5));
    qualifyingHand.add(new Card('C', 3));

    ArrayList<Card> nonQualifyingHand = new ArrayList<>();
    nonQualifyingHand.add(new Card('H', 11)); // Jack
    nonQualifyingHand.add(new Card('D', 9));
    nonQualifyingHand.add(new Card('C', 4));

    assertTrue(GameLogic.isDealerQualified(qualifyingHand));
    assertFalse(GameLogic.isDealerQualified(nonQualifyingHand));
  }

  void testCompareHandsBasic() {
    ArrayList<Card> dealer = new ArrayList<>();
    dealer.add(new Card('H', 10));
    dealer.add(new Card('H', 11));
    dealer.add(new Card('H', 12));

    ArrayList<Card> player = new ArrayList<>();
    player.add(new Card('D', 9));
    player.add(new Card('D', 10));
    player.add(new Card('D', 11));

    assertTrue(GameLogic.compareHands(dealer, player), "Dealer should win with higher straight flush");
  }

  @Test
  void testCompareHandsHighCard() {
    ArrayList<Card> dealer = new ArrayList<>();
    dealer.add(new Card('H', 14)); // Ace
    dealer.add(new Card('D', 10));
    dealer.add(new Card('C', 7));

    ArrayList<Card> player = new ArrayList<>();
    player.add(new Card('S', 13));
    player.add(new Card('H', 9));
    player.add(new Card('D', 6));

    assertTrue(GameLogic.compareHands(dealer, player), "Dealer should win with Ace high");
  }
}