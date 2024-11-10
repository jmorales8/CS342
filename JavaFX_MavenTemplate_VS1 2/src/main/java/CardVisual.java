
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;

public class CardVisual extends StackPane {

    private Rectangle background;
    private VBox content;
    private Group backDesign;
    private Card card;
    private static final double CARD_WIDTH = 120;
    private static final double CARD_HEIGHT = 160;

    public CardVisual(Card card) {
        this.card = card;  // Store the card reference
        setupCard();
    }

    // Getter method to access the card
    public Card getCard() {
        return this.card;
    }
    
    private void setupCard() {
        // Create white card background
        background = new Rectangle(CARD_WIDTH, CARD_HEIGHT);
        background.setFill(Color.WHITE);
        background.setStroke(Color.BLACK);
        background.setArcWidth(10);
        background.setArcHeight(10);

        // Create back design with "Hidden" text and star
        backDesign = createBackDesign();
        backDesign.setVisible(false);

        // Front content setup
        content = new VBox();
        content.setAlignment(Pos.TOP_LEFT);
        content.setPadding(new Insets(5));

        // Top corner
        HBox topCorner = new HBox(2);
        Text valueText = new Text(getValueString());
        Text suitText = new Text(getSuitSymbol());
        valueText.setFont(Font.font("Arial", 24));
        suitText.setFont(Font.font("Arial", 24));
        valueText.setFill(getCardColor());
        suitText.setFill(getCardColor());
        topCorner.getChildren().addAll(valueText, suitText);

        // Bottom corner (rotated)
        HBox bottomCorner = new HBox(2);
        Text bottomValue = new Text(getValueString());
        Text bottomSuit = new Text(getSuitSymbol());
        bottomValue.setFont(Font.font("Arial", 24));
        bottomSuit.setFont(Font.font("Arial", 24));
        bottomValue.setFill(getCardColor());
        bottomSuit.setFill(getCardColor());
        bottomCorner.getChildren().addAll(bottomValue, bottomSuit);
        bottomCorner.setRotate(180);

        Region spacer = new Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        content.getChildren().addAll(topCorner, spacer, bottomCorner);

        getChildren().addAll(background, backDesign, content);
    }

    private Group createBackDesign() {
        Group back = new Group();

        // Blue background
        Rectangle baseRect = new Rectangle(CARD_WIDTH, CARD_HEIGHT);
        baseRect.setFill(Color.rgb(0, 0, 139)); // Dark blue
        baseRect.setArcWidth(10);
        baseRect.setArcHeight(10);

        // Create star in background
        Polygon star = createStar(CARD_WIDTH / 2, CARD_HEIGHT / 2, 30, 15, 5);
        star.setFill(Color.rgb(65, 105, 225, 0.5)); // Light blue, semi-transparent

        // "Hidden" text
        Text hiddenText = new Text("Hidden");
        hiddenText.setFont(Font.font("Arial", FontPosture.ITALIC, 20));
        hiddenText.setFill(Color.WHITE);

        // Center the text
        hiddenText.setX((CARD_WIDTH - hiddenText.getBoundsInLocal().getWidth()) / 2);
        hiddenText.setY((CARD_HEIGHT + hiddenText.getBoundsInLocal().getHeight()) / 2);

        back.getChildren().addAll(baseRect, star, hiddenText);
        return back;
    }

    private Polygon createStar(double centerX, double centerY, double outerRadius, double innerRadius, int numPoints) {
        Polygon star = new Polygon();
        double angle = Math.PI / numPoints;

        for (int i = 0; i < 2 * numPoints; i++) {
            double r = (i % 2 == 0) ? outerRadius : innerRadius;
            star.getPoints().add(centerX + r * Math.cos(i * angle));
            star.getPoints().add(centerY + r * Math.sin(i * angle));
        }

        return star;
    }

    private String getValueString() {
        switch (card.getValue()) {
            case 14:
                return "A";
            case 13:
                return "K";
            case 12:
                return "Q";
            case 11:
                return "J";
            case 10:
                return "10";
            default:
                return String.valueOf(card.getValue());
        }
    }

    private String getSuitSymbol() {
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
        content.setVisible(false);
        backDesign.setVisible(true);
        background.setVisible(false);
    }

    public void setFaceUp() {
        content.setVisible(true);
        backDesign.setVisible(false);
        background.setVisible(true);
    }
}
