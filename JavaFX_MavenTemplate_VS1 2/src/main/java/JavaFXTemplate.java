
import java.util.ArrayList;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class JavaFXTemplate extends Application {

    private Stage primaryStage;
    private Scene startScene;
    private Scene gameScene;
    private Player playerOne;
    private Player playerTwo;
    private Dealer theDealer;

    // Game state
    private enum GameState {
        PLAYER_ONE_TURN,
        PLAYER_TWO_TURN,
        ROUND_COMPLETE
    }
    private GameState currentState;

    // Game UI components
    private Button dealButton;
    private VBox player1Area;
    private VBox player2Area;
    private HBox dealerCards;
    private HBox player1Cards;
    private HBox player2Cards;
    private Label gameInfoLabel;

    // Player 1 controls
    private TextField player1AnteField;
    private TextField player1PairPlusField;
    private Button player1PlayButton;
    private Button player1FoldButton;

    // Player 2 controls
    private TextField player2AnteField;
    private TextField player2PairPlusField;
    private Button player2PlayButton;
    private Button player2FoldButton;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Three Card Poker");

        // Initialize game objects
        playerOne = new Player();
        playerTwo = new Player();
        theDealer = new Dealer();

        createStartScreen();
        createGameScreen();

        primaryStage.setScene(startScene);
        primaryStage.show();
    }

    private void createStartScreen() {
        VBox startRoot = new VBox(20);
        startRoot.setAlignment(Pos.CENTER);
        startRoot.setStyle("-fx-background-color: #1a1a1a;");

        Text titleText = new Text("Welcome to Three Card Poker");
        titleText.setFont(new Font("Arial", 36));
        titleText.setStyle("-fx-fill: white;");

        Button startButton = new Button("Start Game");
        Button exitButton = new Button("Exit");

        String buttonStyle = "-fx-background-color: #4CAF50; -fx-text-fill: white; "
                + "-fx-font-size: 16px; -fx-padding: 10 20 10 20; -fx-min-width: 200px;";
        startButton.setStyle(buttonStyle);
        exitButton.setStyle(buttonStyle.replace("#4CAF50", "#f44336"));

        startButton.setOnAction(e -> showGameScreen());
        exitButton.setOnAction(e -> Platform.exit());

        startRoot.getChildren().addAll(titleText, startButton, exitButton);
        startScene = new Scene(startRoot, 1000, 800);
    }

    private void createGameScreen() {
        BorderPane gameRoot = new BorderPane();
        gameRoot.setStyle("-fx-background-color: #1a1a1a;");

        // Create ScrollPane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle(
                "-fx-background: #1a1a1a; "
                + "-fx-background-color: #1a1a1a; "
                + "-fx-control-inner-background: #1a1a1a;"
        );
        scrollPane.setFitToWidth(true);  // Makes content fit the width of the scroll pane

        // Set ScrollPane styling programmatically
        String scrollPaneStyle
                = ".scroll-pane { -fx-background-color: #1a1a1a; }"
                + ".scroll-pane > .viewport { -fx-background-color: #1a1a1a; }"
                + ".scroll-bar:vertical, .scroll-bar:horizontal { -fx-background-color: #1a1a1a; }"
                + ".scroll-bar:vertical .thumb, .scroll-bar:horizontal .thumb { "
                + "    -fx-background-color: #333333; -fx-background-radius: 5em; }"
                + ".scroll-bar:vertical .track, .scroll-bar:horizontal .track { "
                + "    -fx-background-color: #1a1a1a; -fx-border-color: #1a1a1a; }"
                + ".scroll-bar .increment-button, .scroll-bar .decrement-button { "
                + "    -fx-background-color: #1a1a1a; -fx-border-color: #1a1a1a; }"
                + ".scroll-bar .increment-arrow, .scroll-bar .decrement-arrow { "
                + "    -fx-background-color: white; }";

        // Main content container
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setStyle("-fx-background-color: #1a1a1a;");

        // Dealer area at top
        VBox dealerArea = new VBox(10);
        dealerArea.setAlignment(Pos.CENTER);
        Label dealerLabel = new Label("Dealer's Cards");
        dealerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        dealerCards = new HBox(10);
        dealerCards.setAlignment(Pos.CENTER);
        dealerArea.getChildren().addAll(dealerLabel, dealerCards);

        // Game info display
        gameInfoLabel = new Label("Game Info");
        gameInfoLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-size: 18px;");
        dealerArea.getChildren().add(gameInfoLabel);

        // Player areas
        setupPlayerAreas();

        // Top bar with back button and deal button
        VBox topBar = new VBox(10);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(10));
        Button backButton = new Button("Back to Menu");
        backButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        backButton.setOnAction(e -> showStartScreen());

        dealButton = new Button("Deal");
        dealButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        dealButton.setOnAction(e -> startNewRound());

        topBar.getChildren().addAll(backButton, dealButton);

        // Add everything to the main content
        mainContent.getChildren().addAll(dealerArea, player1Area, player2Area);

        // Set up the scroll pane
        scrollPane.setContent(mainContent);

        // Add components to the root
        gameRoot.setTop(topBar);
        gameRoot.setCenter(scrollPane);

        gameScene = new Scene(gameRoot, 1000, 800);

        // Add the styles directly to the scene
        gameScene.getStylesheets().add("data:text/css," + scrollPaneStyle.replace(" ", "%20"));
    }

    private void setupPlayerAreas() {
        // Player 1 Area
        player1Area = new VBox(10);
        player1Area.setStyle("-fx-padding: 10px; -fx-border-color: #333333;");
        Label player1Label = new Label("Player 1 Cards");
        player1Label.setStyle("-fx-text-fill: white;");
        player1Cards = new HBox(10);
        player1AnteField = createStyledTextField("Enter Ante Bet");
        player1PairPlusField = createStyledTextField("Enter Pair Plus Bet");
        player1PlayButton = createStyledButton("Play", "#4CAF50");
        player1FoldButton = createStyledButton("Fold", "#f44336");

        player1Area.getChildren().addAll(
                player1Label,
                player1Cards,
                new Label("Ante:"), player1AnteField,
                new Label("Pair+:"), player1PairPlusField,
                new HBox(10, player1PlayButton, player1FoldButton)
        );

        // Add event handlers for Player 1 buttons
        player1PlayButton.setOnAction(e -> handlePlayer1Action(true));
        player1FoldButton.setOnAction(e -> handlePlayer1Action(false));

        // Player 2 Area
        player2Area = new VBox(10);
        player2Area.setStyle("-fx-padding: 10px; -fx-border-color: #333333;");
        Label player2Label = new Label("Player 2 Cards");
        player2Label.setStyle("-fx-text-fill: white;");
        player2Cards = new HBox(10);
        player2AnteField = createStyledTextField("Enter Ante Bet");
        player2PairPlusField = createStyledTextField("Enter Pair Plus Bet");
        player2PlayButton = createStyledButton("Play", "#4CAF50");
        player2FoldButton = createStyledButton("Fold", "#f44336");

        player2Area.getChildren().addAll(
                player2Label,
                player2Cards,
                new Label("Ante:"), player2AnteField,
                new Label("Pair+:"), player2PairPlusField,
                new HBox(10, player2PlayButton, player2FoldButton)
        );

        // Add event handlers for Player 2 buttons
        player2PlayButton.setOnAction(e -> handlePlayer2Action(true));
        player2FoldButton.setOnAction(e -> handlePlayer2Action(false));

        // Set initial states
        setControlsDisabled(true);
    }

    private void startNewRound() {
        // Deal cards
        ArrayList<Card> dealerHand = theDealer.dealHand();
        ArrayList<Card> player1Hand = theDealer.dealHand();
        ArrayList<Card> player2Hand = theDealer.dealHand();

        // Display dealer's hand FACE UP (changed from face down)
        displayHand(dealerCards, dealerHand, false);  // Changed to false to show face up
        displayHand(player1Cards, player1Hand, false);
        displayHand(player2Cards, player2Hand, true); // Player 2's hand face down initially

        // Set game state
        currentState = GameState.PLAYER_ONE_TURN;
        updateGameState();

        // Disable deal button
        dealButton.setDisable(true);
    }

    private void updateGameState() {
        switch (currentState) {
            case PLAYER_ONE_TURN:
                gameInfoLabel.setText("Player 1's Turn");
                // Enable Player 1 controls, disable Player 2
                setPlayer1ControlsEnabled(true);
                setPlayer2ControlsEnabled(false);
                // Show Player 1's cards, hide Player 2's
                setCardsVisible(player1Cards, true);
                setCardsVisible(player2Cards, false);
                // Dealer's cards remain visible - no need to change them
                break;

            case PLAYER_TWO_TURN:
                gameInfoLabel.setText("Player 2's Turn");
                // Enable Player 2 controls, disable Player 1
                setPlayer1ControlsEnabled(false);
                setPlayer2ControlsEnabled(true);
                // Show Player 2's cards, hide Player 1's
                setCardsVisible(player1Cards, false);
                setCardsVisible(player2Cards, true);
                // Dealer's cards remain visible - no need to change them
                break;

            case ROUND_COMPLETE:
                gameInfoLabel.setText("Round Complete");
                // Show all player cards
                setCardsVisible(player1Cards, true);
                setCardsVisible(player2Cards, true);
                // Dealer's cards are already visible
                // Disable all controls
                setControlsDisabled(true);
                // Enable deal button for next round
                dealButton.setDisable(false);
                break;
        }
    }

    private void handlePlayer1Action(boolean isPlay) {
        if (isPlay) {
            // Handle play action
            try {
                playerOne.anteBet = Integer.parseInt(player1AnteField.getText());
                playerOne.pairPlusBet = Integer.parseInt(player1PairPlusField.getText());
            } catch (NumberFormatException e) {
                gameInfoLabel.setText("Please enter valid bets");
                return;
            }
        }
        // Move to player 2's turn
        currentState = GameState.PLAYER_TWO_TURN;
        updateGameState();
    }

    private void handlePlayer2Action(boolean isPlay) {
        if (isPlay) {
            // Handle play action
            try {
                playerTwo.anteBet = Integer.parseInt(player2AnteField.getText());
                playerTwo.pairPlusBet = Integer.parseInt(player2PairPlusField.getText());
            } catch (NumberFormatException e) {
                gameInfoLabel.setText("Please enter valid bets");
                return;
            }
        }
        // Complete the round
        currentState = GameState.ROUND_COMPLETE;
        updateGameState();
    }

    private void setControlsDisabled(boolean disabled) {
        setPlayer1ControlsEnabled(!disabled);
        setPlayer2ControlsEnabled(!disabled);
    }

    private void setPlayer1ControlsEnabled(boolean enabled) {
        player1AnteField.setDisable(!enabled);
        player1PairPlusField.setDisable(!enabled);
        player1PlayButton.setDisable(!enabled);
        player1FoldButton.setDisable(!enabled);
    }

    private void setPlayer2ControlsEnabled(boolean enabled) {
        player2AnteField.setDisable(!enabled);
        player2PairPlusField.setDisable(!enabled);
        player2PlayButton.setDisable(!enabled);
        player2FoldButton.setDisable(!enabled);
    }

    private void setCardsVisible(HBox cardArea, boolean visible) {
        cardArea.getChildren().forEach(node -> {
            if (node instanceof CardVisual) {
                if (visible) {
                    ((CardVisual) node).setFaceUp();
                } else {
                    ((CardVisual) node).setFaceDown();
                }
            }
        });
    }

    private TextField createStyledTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setStyle("-fx-background-color: #333333; -fx-text-fill: white;");
        return field;
    }

    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white;");
        return button;
    }

    private void displayHand(HBox cardArea, ArrayList<Card> hand, boolean faceDown) {
        cardArea.getChildren().clear();
        for (Card card : hand) {
            CardVisual cardView = new CardVisual(card);
            if (faceDown) {
                cardView.setFaceDown();
            }
            cardArea.getChildren().add(cardView);
        }
    }

    private void showGameScreen() {
        primaryStage.setScene(gameScene);
        primaryStage.setTitle("Three Card Poker - Game");
    }

    private void showStartScreen() {
        primaryStage.setScene(startScene);
        primaryStage.setTitle("Three Card Poker");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
