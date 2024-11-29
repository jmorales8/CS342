import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class CardVisual extends StackPane {
    private Rectangle background;
    private Text displayText;
    private Card card;

    public CardVisual(Card card) {
        this.card = card;

        // Create card background
        background = new Rectangle(80, 120);
        background.setFill(Color.WHITE);
        background.setStroke(Color.BLACK);
        background.setArcWidth(10);
        background.setArcHeight(10);

        // Create card text
        displayText = new Text(getDisplayString());
        displayText.setFont(new Font(18));
        displayText.setFill(getCardColor());

        // Add elements to StackPane
        getChildren().addAll(background, displayText);
        setAlignment(Pos.CENTER);
    }

    private String getDisplayString() {
        String valueStr = getValueString();
        String suitStr = getSuitSymbol(card);
        return valueStr + "\n" + suitStr;
    }

    private String getValueString() {
        int value = card.getValue();
        switch (value) {
            case 14:
                return "A";
            case 13:
                return "K";
            case 12:
                return "Q";
            case 11:
                return "J";
            default:
                return String.valueOf(value);
        }
    }

    private String getSuitSymbol(Card card) {
        switch (card.getSuit()) {
            case 'H':
                return "♥";
            case 'D':
                return "♦";
            case 'C':
                return "♣";
            case 'S':
                return "♠";
            default:
                return String.valueOf(card.getSuit());
        }
    }

    private Color getCardColor() {
        return (card.getSuit() == 'H' || card.getSuit() == 'D') ? Color.RED : Color.BLACK;
    }

    public void setFaceDown() {
        background.setFill(Color.LIGHTGRAY);
        displayText.setVisible(false);
    }

    public void setFaceUp() {
        background.setFill(Color.WHITE);
        displayText.setVisible(true);
    }
}