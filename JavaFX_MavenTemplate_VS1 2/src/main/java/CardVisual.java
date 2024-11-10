import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class CardVisual extends StackPane {
    private Rectangle background;
    private VBox frontContent;
    private Text backText;
    private final Card card;
    private static final double CARD_WIDTH = 100;
    private static final double CARD_HEIGHT = 140;

    public CardVisual(Card card) {
        this.card = card;
        setupCard();
    }

    private void setupCard() {
        // Create card background
        background = new Rectangle(CARD_WIDTH, CARD_HEIGHT);
        background.setFill(Color.WHITE);
        background.setStroke(Color.BLACK);
        background.setArcWidth(10);
        background.setArcHeight(10);

        // Create front content
        frontContent = new VBox(5);
        frontContent.setAlignment(Pos.CENTER);

        // Top value and suit
        Text valueText = new Text(getValueString());
        Text suitText = new Text(getSuitSymbol());
        valueText.setFont(Font.font("Arial", 20));
        suitText.setFont(Font.font("Arial", 20));

        // Set color based on suit
        Color cardColor = (card.getSuit() == 'H' || card.getSuit() == 'D') ? Color.RED : Color.BLACK;
        valueText.setFill(cardColor);
        suitText.setFill(cardColor);

        // Center suit
        Text centerSuit = new Text(getSuitSymbol());
        centerSuit.setFont(Font.font("Arial", 40));
        centerSuit.setFill(cardColor);

        frontContent.getChildren().addAll(valueText, centerSuit, suitText);

        // Create back content (Hidden text)
        backText = new Text("Hidden");
        backText.setFont(Font.font("Arial", 20));
        backText.setFill(Color.WHITE);
        backText.setVisible(false);

        // Add all elements
        getChildren().addAll(background, frontContent, backText);
        setAlignment(Pos.CENTER);
    }

    private String getValueString() {
        switch(card.getValue()) {
            case 14: return "A";
            case 13: return "K";
            case 12: return "Q";
            case 11: return "J";
            default: return String.valueOf(card.getValue());
        }
    }

    private String getSuitSymbol() {
        switch(card.getSuit()) {
            case 'H': return "♥";
            case 'D': return "♦";
            case 'C': return "♣";
            case 'S': return "♠";
            default: return String.valueOf(card.getSuit());
        }
    }

    public void setFaceDown() {
        frontContent.setVisible(false);
        backText.setVisible(true);
        background.setFill(Color.BLUE);
        background.setStroke(Color.WHITE);
    }

    public void setFaceUp() {
        frontContent.setVisible(true);
        backText.setVisible(false);
        background.setFill(Color.WHITE);
        background.setStroke(Color.BLACK);
    }

    public Card getCard() {
        return this.card;
    }
}
