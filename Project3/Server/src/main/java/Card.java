import java.io.Serializable;

public class Card implements Serializable {
  private static final long serialVersionUID = 1L;

  private char suit;
  private int value;

  public Card(char suit, int value) {
    this.suit = suit;
    this.value = value;
  }

  public char getSuit() {
    return suit;
  }

  public int getValue() {
    return value;
  }
}
