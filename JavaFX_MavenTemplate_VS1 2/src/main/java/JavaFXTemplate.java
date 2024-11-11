
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class JavaFXTemplate extends Application {

    private Stage primaryStage;
    private Scene startScene;
    private Scene gameScene;
    private Player playerOne;
    private Player playerTwo;
    private Dealer theDealer;
    private GameState currentState;

    // Game state
    private enum GameState {
        PLAYER_ONE_TURN,
        PLAYER_TWO_TURN,
        ROUND_COMPLETE
    }

    private ArrayList<Card> dealerHand;
    private ArrayList<Card> player1Hand;
    private ArrayList<Card> player2Hand;

    // UI Components
    private Button dealButton;
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

    // Additional UI elements
    private HBox playAgainBox;
    private VBox player1Area;
    private VBox player2Area;
    private Label player1TotalWinningsLabel;
    private Label player2TotalWinningsLabel;
    private Scene exitScene;
    private String currentTheme = "default";
    private int player1PushedAntes = 0;
    private int player2PushedAntes = 0;
    private Label player1PushedAntesLabel;
    private Label player2PushedAntesLabel;
    private MenuItem newLookItem;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Three Card Poker");

        // Initialize game objects
        playerOne = new Player();
        playerTwo = new Player();
        theDealer = new Dealer();

        // Load CSS first
        String css = getClass().getResource("/css/dark-theme.css").toExternalForm();

        createStartScreen();
        createGameScreen();

        // Apply CSS to both scenes
        startScene.getStylesheets().add(css);
        gameScene.getStylesheets().add(css);

        // Set initial theme
        currentTheme = "default";

        primaryStage.setScene(startScene);
        primaryStage.show();
    }

    private void loadStyles(Scene scene) {
        String[] stylesheets = {
            "/css/buttons.css",
            "/css/inputs.css",
            "/css/labels.css",
            "/css/containers.css",
            "/css/menu.css"
        };

        for (String stylesheet : stylesheets) {
            String css = getClass().getResource(stylesheet).toExternalForm();
            scene.getStylesheets().add(css);
        }
    }

    private void toggleTheme() {
        // Clear existing stylesheets
        gameScene.getStylesheets().clear();
        startScene.getStylesheets().clear();

        if ("default".equals(currentTheme)) {
            // Switch to light theme
            currentTheme = "light";
            loadTheme("/css/light-theme.css");
            newLookItem.setText("Switch to Dark Theme");
        } else {
            // Switch to dark theme
            currentTheme = "default";
            loadTheme("/css/dark-theme.css");
            newLookItem.setText("Switch to Light Theme");
        }
    }

    private void loadTheme(String themePath) {
        try {
            String css = getClass().getResource(themePath).toExternalForm();
            // Apply to both scenes
            gameScene.getStylesheets().add(css);
            startScene.getStylesheets().add(css);
        } catch (Exception e) {
            System.err.println("Failed to load theme: " + themePath);
            e.printStackTrace();
        }
    }

    private void createStartScreen() {
        VBox startRoot = new VBox(20);
        startRoot.setAlignment(Pos.CENTER);
        startRoot.getStyleClass().add("root");

        Text titleText = new Text("Welcome to Three Card Poker");
        titleText.getStyleClass().add("title-text");

        Button startButton = new Button("Start Game");
        startButton.getStyleClass().add("primary-button");

        Button exitButton = new Button("Exit");
        exitButton.getStyleClass().add("danger-button");

        startButton.setOnAction(e -> {
            showGameScreen();
            startNewRound();
        });
        exitButton.setOnAction(e -> Platform.exit());

        startRoot.getChildren().addAll(titleText, startButton, exitButton);
        startScene = new Scene(startRoot, 1200, 800);
    }

    private void createGameScreen() {
        BorderPane gameRoot = new BorderPane();
        gameRoot.getStyleClass().add("root");

        // Create ScrollPane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setFitToWidth(true);

        // Main content container
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.getStyleClass().add("root");

        // Dealer area at top with title
        VBox dealerArea = new VBox(10);
        dealerArea.setAlignment(Pos.CENTER);
        Label dealerLabel = new Label("Dealer's Cards");
        dealerLabel.getStyleClass().add("dealer-label");
        dealerCards = new HBox(10);
        dealerCards.setAlignment(Pos.CENTER);
        dealerCards.getStyleClass().add("card-container");
        dealerArea.getChildren().addAll(dealerLabel, dealerCards);

        // Game info display
        gameInfoLabel = new Label("Players: Place your bets");
        gameInfoLabel.getStyleClass().add("game-info-label");
        dealerArea.getChildren().add(gameInfoLabel);

        // Create horizontal container for both players
        HBox playersContainer = new HBox(50);
        playersContainer.setAlignment(Pos.CENTER);

        // Player 1 Area
        player1Area = new VBox(10);
        player1Area.getStyleClass().add("player-area");
        Label player1Label = new Label("Player 1 Cards");
        player1Label.getStyleClass().add("player-label");
        player1Cards = new HBox(10);
        player1Cards.getStyleClass().add("card-container");

        player1AnteField = createStyledTextField("Enter Ante Bet");
        player1PairPlusField = createStyledTextField("Enter Pair Plus Bet");
        player1PlayButton = createStyledButton("Play", "#4CAF50");
        player1FoldButton = createStyledButton("Fold", "#f44336");

        player1TotalWinningsLabel = new Label("Total Winnings: $0");
        player1TotalWinningsLabel.getStyleClass().add("winnings-label");

        player1PushedAntesLabel = new Label("Pushed Antes: $0");
        player1PushedAntesLabel.getStyleClass().add("pushed-antes-label");

        HBox player1Buttons = new HBox(10, player1PlayButton, player1FoldButton);
        player1Buttons.getStyleClass().add("button-container");

        Label anteLabel1 = new Label("Ante:");
        Label pairPlusLabel1 = new Label("Pair+:");
        anteLabel1.getStyleClass().add("player-label");
        pairPlusLabel1.getStyleClass().add("player-label");

        player1Area.getChildren().addAll(
                player1Label,
                player1TotalWinningsLabel,
                player1PushedAntesLabel,
                player1Cards,
                anteLabel1, player1AnteField,
                pairPlusLabel1, player1PairPlusField,
                player1Buttons
        );

        // Player 2 Area
        player2Area = new VBox(10);
        player2Area.getStyleClass().add("player-area");
        Label player2Label = new Label("Player 2 Cards");
        player2Label.getStyleClass().add("player-label");
        player2Cards = new HBox(10);
        player2Cards.getStyleClass().add("card-container");

        player2AnteField = createStyledTextField("Enter Ante Bet");
        player2PairPlusField = createStyledTextField("Enter Pair Plus Bet");
        player2PlayButton = createStyledButton("Play", "#4CAF50");
        player2FoldButton = createStyledButton("Fold", "#f44336");

        player2TotalWinningsLabel = new Label("Total Winnings: $0");
        player2TotalWinningsLabel.getStyleClass().add("winnings-label");

        player2PushedAntesLabel = new Label("Pushed Antes: $0");
        player2PushedAntesLabel.getStyleClass().add("pushed-antes-label");

        HBox player2Buttons = new HBox(10, player2PlayButton, player2FoldButton);
        player2Buttons.getStyleClass().add("button-container");

        Label anteLabel2 = new Label("Ante:");
        Label pairPlusLabel2 = new Label("Pair+:");
        anteLabel2.getStyleClass().add("player-label");
        pairPlusLabel2.getStyleClass().add("player-label");

        player2Area.getChildren().addAll(
                player2Label,
                player2TotalWinningsLabel,
                player2PushedAntesLabel,
                player2Cards,
                anteLabel2, player2AnteField,
                pairPlusLabel2, player2PairPlusField,
                player2Buttons
        );

        // Add players to horizontal container
        playersContainer.getChildren().addAll(player1Area, player2Area);

        // Create deal button and play again label
        dealButton = new Button("Deal");
        dealButton.getStyleClass().add("primary-button");
        Label playAgainLabel = new Label("Play Again?");
        playAgainLabel.getStyleClass().add("player-label");

        // Create an HBox to hold the play again label and deal button
        playAgainBox = new HBox(10);
        playAgainBox.setAlignment(Pos.CENTER);
        playAgainBox.getChildren().addAll(playAgainLabel, dealButton);
        playAgainBox.setVisible(false);

        // Top bar setup
        VBox topBar = new VBox(10);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(10));

        Button backButton = new Button("Back to Menu");
        backButton.getStyleClass().add("danger-button");
        backButton.setOnAction(e -> showStartScreen());

        dealButton.setOnAction(e -> {
            playAgainBox.setVisible(false);
            startNewRound();
        });

        topBar.getChildren().addAll(backButton, playAgainBox);

        // Add button actions
        player1PlayButton.setOnAction(e -> handlePlayer1Action(true));
        player1FoldButton.setOnAction(e -> handlePlayer1Action(false));
        player2PlayButton.setOnAction(e -> handlePlayer2Action(true));
        player2FoldButton.setOnAction(e -> handlePlayer2Action(false));

        // Add everything to the main content
        mainContent.getChildren().addAll(dealerArea, playersContainer);

        // Set up the scroll pane
        scrollPane.setContent(mainContent);

        // Add components to the root
        gameRoot.setTop(topBar);
        gameRoot.setCenter(scrollPane);

        // Create game scene
        gameScene = new Scene(gameRoot, 1200, 800);

        // Menu setup
        MenuBar menuBar = new MenuBar();
        menuBar.getStyleClass().add("menu-bar");
        menuBar.setUseSystemMenuBar(false);  // Add this line to ensure consistent styling
        
        Menu optionsMenu = new Menu("Options");
        optionsMenu.getStyleClass().add("menu");

        MenuItem exitItem = new MenuItem("Exit");
        MenuItem freshStartItem = new MenuItem("Fresh Start");
        newLookItem = new MenuItem("Switch to Light Theme");  // Initialize the class field here

        // Add styles to menu items
        Arrays.asList(exitItem, freshStartItem, newLookItem)
                .forEach(item -> item.getStyleClass().add("menu-item"));

        // Add event handlers
        exitItem.setOnAction(e -> showExitScreen());
        freshStartItem.setOnAction(e -> resetGame());
        newLookItem.setOnAction(e -> toggleTheme());

        optionsMenu.getItems().addAll(exitItem, freshStartItem, newLookItem);
        menuBar.getMenus().add(optionsMenu);

        // Add MenuBar to top of BorderPane
        VBox topContainer = new VBox();
        topContainer.setSpacing(0);  // Set spacing to 0
        topContainer.getChildren().addAll(menuBar, playAgainBox);
        gameRoot.setTop(topContainer);
    }

    private void updateTotalWinningsLabelStyles() {
        String winningsStyle = "-fx-text-fill: #00ff00; -fx-font-size: 14px; -fx-font-weight: bold;";
        player1TotalWinningsLabel.setStyle(winningsStyle);
        player2TotalWinningsLabel.setStyle(winningsStyle);
    }

    private void showExitScreen() {
        if (exitScene == null) {
            createExitScreen();
        }
        primaryStage.setScene(exitScene);
    }

    private void createExitScreen() {
        VBox exitRoot = new VBox(20);  // 20 pixels spacing between elements
        exitRoot.setAlignment(Pos.CENTER);
        exitRoot.setStyle("-fx-background-color: #1a1a1a;");
        exitRoot.setPadding(new Insets(20));

        // Title
        Text exitText = new Text("Are you sure you want to exit?");
        exitText.setFont(new Font("Arial", 28));
        exitText.setFill(Color.WHITE);

        // Subtitle with current standings (optional)
        Text standingsText = new Text(String.format(
                "Current Standings:\nPlayer 1: $%d\nPlayer 2: $%d",
                playerOne.totalWinnings,
                playerTwo.totalWinnings
        ));
        standingsText.setFont(new Font("Arial", 16));
        standingsText.setFill(Color.LIGHTGRAY);
        standingsText.setTextAlignment(TextAlignment.CENTER);

        // Buttons container
        HBox buttonBox = new HBox(20);  // 20 pixels spacing between buttons
        buttonBox.setAlignment(Pos.CENTER);

        // Continue button
        Button continueButton = new Button("Continue Playing");
        continueButton.setStyle(
                "-fx-background-color: #4CAF50;"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 16px;"
                + "-fx-padding: 10 20 10 20;"
                + "-fx-min-width: 150px;"
                + "-fx-cursor: hand;"
        );
        continueButton.setOnAction(e -> returnToGame());
        continueButton.setOnMouseEntered(e
                -> continueButton.setStyle(
                        "-fx-background-color: #45a049;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-size: 16px;"
                        + "-fx-padding: 10 20 10 20;"
                        + "-fx-min-width: 150px;"
                        + "-fx-cursor: hand;"
                )
        );
        continueButton.setOnMouseExited(e
                -> continueButton.setStyle(
                        "-fx-background-color: #4CAF50;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-size: 16px;"
                        + "-fx-padding: 10 20 10 20;"
                        + "-fx-min-width: 150px;"
                        + "-fx-cursor: hand;"
                )
        );

        // Quit button
        Button quitButton = new Button("Quit Game");
        quitButton.setStyle(
                "-fx-background-color: #f44336;"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 16px;"
                + "-fx-padding: 10 20 10 20;"
                + "-fx-min-width: 150px;"
                + "-fx-cursor: hand;"
        );
        quitButton.setOnAction(e -> Platform.exit());
        quitButton.setOnMouseEntered(e
                -> quitButton.setStyle(
                        "-fx-background-color: #d32f2f;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-size: 16px;"
                        + "-fx-padding: 10 20 10 20;"
                        + "-fx-min-width: 150px;"
                        + "-fx-cursor: hand;"
                )
        );
        quitButton.setOnMouseExited(e
                -> quitButton.setStyle(
                        "-fx-background-color: #f44336;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-size: 16px;"
                        + "-fx-padding: 10 20 10 20;"
                        + "-fx-min-width: 150px;"
                        + "-fx-cursor: hand;"
                )
        );

        // Add buttons to button container
        buttonBox.getChildren().addAll(continueButton, quitButton);

        // Add all elements to root container
        exitRoot.getChildren().addAll(exitText, standingsText, buttonBox);

        // Create scene with black background
        exitScene = new Scene(exitRoot, 500, 300);

        // Add hover effect cursor for entire scene
        exitRoot.setOnMouseEntered(e
                -> exitScene.setCursor(Cursor.DEFAULT)
        );
    }

    private void returnToGame() {
        primaryStage.setScene(gameScene);
    }

// Add this new method to update the pushed antes displays:
    private void updatePushedAntesDisplay() {
        player1PushedAntesLabel.setText(String.format("Pushed Antes: $%d", player1PushedAntes));
        player2PushedAntesLabel.setText(String.format("Pushed Antes: $%d", player2PushedAntes));
    }

// Update resetGame() to also reset pushed antes:
    private void resetGame() {
        playerOne.totalWinnings = 0;
        playerTwo.totalWinnings = 0;
        player1PushedAntes = 0;
        player2PushedAntes = 0;
        updateWinningsDisplays();
        updatePushedAntesDisplay();
        startNewRound();
    }

    private TextField createStyledTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("styled-textfield");
        return field;
    }

    // Modified button creation method
    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.getStyleClass().clear();  // Clear default styles
        button.getStyleClass().add("game-button");

        switch (text) {
            case "Play":
                button.getStyleClass().add("play-button");
                break;
            case "Fold":
                button.getStyleClass().add("fold-button");
                break;
            default:
                button.getStyleClass().add("default-button");
                break;
        }

        return button;
    }

    private boolean player1InitialBetMade = false;
    private boolean player2InitialBetMade = false;
    private boolean player1PlayDecisionMade = false;
    private boolean player2PlayDecisionMade = false;

    private void handlePlayer1Action(boolean isPlay) {
        if (!player1InitialBetMade) {
            // Handle initial ante and pair plus bets
            try {
                int anteBet = Integer.parseInt(player1AnteField.getText());
                int pairPlusBet = 0;

                // Validate ante bet
                if (anteBet < 5 || anteBet > 25) {
                    gameInfoLabel.setText("Ante bet must be between $5 and $25");
                    return;
                }

                // Validate pair plus bet if entered
                if (!player1PairPlusField.getText().isEmpty()) {
                    pairPlusBet = Integer.parseInt(player1PairPlusField.getText());
                    if (pairPlusBet < 5 || pairPlusBet > 25) {
                        gameInfoLabel.setText("Pair Plus bet must be between $5 and $25");
                        return;
                    }
                }

                playerOne.anteBet = anteBet;
                playerOne.pairPlusBet = pairPlusBet;
                player1InitialBetMade = true;

                // Update UI for play decision
                player1AnteField.setDisable(true);
                player1PairPlusField.setDisable(true);
                player1PlayButton.setText("Make Play Bet (" + anteBet + ")");
                player1FoldButton.setText("Fold");

                // Show player's cards
                revealPlayer1Cards();

                checkInitialBets();

            } catch (NumberFormatException e) {
                gameInfoLabel.setText("Please enter valid bets");
                return;
            }
        } else {
            // Handle play decision
            if (isPlay) {
                playerOne.playBet = playerOne.anteBet;
            } else {
                playerOne.anteBet = 0;
                playerOne.playBet = 0;
                playerOne.pairPlusBet = 0;
            }

            player1PlayDecisionMade = true;
            // Instead of disabling, we'll keep the buttons enabled but styled as disabled
            player1PlayButton.setDisable(true);  // This will trigger the CSS disabled state
            player1FoldButton.setDisable(true);   // This will trigger the CSS disabled state

            // Optional: Add a visual indicator that the decision is locked in
            player1PlayButton.setText(isPlay ? "Play Bet Made" : "Folded");
            player1FoldButton.setText(isPlay ? "Play Bet Made" : "Folded");

            checkPlayDecisions();
        }
    }

    private void handlePlayer2Action(boolean isPlay) {
        if (!player2InitialBetMade) {
            // Handle initial ante and pair plus bets
            try {
                int anteBet = Integer.parseInt(player2AnteField.getText());
                int pairPlusBet = 0;

                // Validate ante bet
                if (anteBet < 5 || anteBet > 25) {
                    gameInfoLabel.setText("Ante bet must be between $5 and $25");
                    return;
                }

                // Validate pair plus bet if entered
                if (!player2PairPlusField.getText().isEmpty()) {
                    pairPlusBet = Integer.parseInt(player2PairPlusField.getText());
                    if (pairPlusBet < 5 || pairPlusBet > 25) {
                        gameInfoLabel.setText("Pair Plus bet must be between $5 and $25");
                        return;
                    }
                }

                playerTwo.anteBet = anteBet;
                playerTwo.pairPlusBet = pairPlusBet;
                player2InitialBetMade = true;

                // Update UI for play decision
                player2AnteField.setDisable(true);
                player2PairPlusField.setDisable(true);
                player2PlayButton.setText("Make Play Bet (" + anteBet + ")");
                player2FoldButton.setText("Fold");

                // Show player's cards
                revealPlayer2Cards();

                checkInitialBets();

            } catch (NumberFormatException e) {
                gameInfoLabel.setText("Please enter valid bets");
                return;
            }
        } else {
            // Handle play decision
            if (isPlay) {
                playerTwo.playBet = playerTwo.anteBet;
            } else {
                playerTwo.anteBet = 0;
                playerTwo.playBet = 0;
                playerTwo.pairPlusBet = 0;
            }

            player2PlayDecisionMade = true;
            // Instead of disabling, we'll keep the buttons enabled but styled as disabled
            player2PlayButton.setDisable(true);  // This will trigger the CSS disabled state
            player2FoldButton.setDisable(true);   // This will trigger the CSS disabled state

            // Optional: Add a visual indicator that the decision is locked in
            player2PlayButton.setText(isPlay ? "Play Bet Made" : "Folded");
            player2FoldButton.setText(isPlay ? "Play Bet Made" : "Folded");

            checkPlayDecisions();
        }
    }

    private void revealPlayer1Cards() {
        for (javafx.scene.Node node : player1Cards.getChildren()) {
            if (node instanceof CardVisual) {
                ((CardVisual) node).setFaceUp();
            }
        }
    }

    private void revealPlayer2Cards() {
        for (javafx.scene.Node node : player2Cards.getChildren()) {
            if (node instanceof CardVisual) {
                ((CardVisual) node).setFaceUp();
            }
        }
    }

    private void checkInitialBets() {
        if (player1InitialBetMade && player2InitialBetMade) {
            gameInfoLabel.setText("Players: Review your cards and make play bet or fold");
        }
    }

    private void checkPlayDecisions() {
        if (player1PlayDecisionMade && player2PlayDecisionMade) {
            // Reveal dealer's cards and evaluate the round
            revealDealerCards();
            evaluateRound();
        }
    }

    private boolean checkDealerQualifies() {
        // Create ArrayList of dealer's current cards
        ArrayList<Integer> values = new ArrayList<>();
        for (javafx.scene.Node node : dealerCards.getChildren()) {
            if (node instanceof CardVisual) {
                Card card = ((CardVisual) node).getCard();
                values.add(card.getValue());
            }
        }
        Collections.sort(values, Collections.reverseOrder());
        return values.get(0) >= 12; // Queen or better (12 = Queen, 13 = King, 14 = Ace)
    }

    private void startNewRound() {
        // Reset all betting flags
        player1InitialBetMade = false;
        player2InitialBetMade = false;
        player1PlayDecisionMade = false;
        player2PlayDecisionMade = false;

        // Deal exactly 3 cards
        dealerHand = theDealer.dealHand();  // dealHand() should return 3 cards
        player1Hand = theDealer.dealHand();  // dealHand() should return 3 cards
        player2Hand = theDealer.dealHand();  // dealHand() should return 3 cards

        // Clear and reset displays
        dealerCards.getChildren().clear();
        player1Cards.getChildren().clear();
        player2Cards.getChildren().clear();

        // Add exactly 3 cards face down
        for (int i = 0; i < 3; i++) {  // Loop exactly 3 times
            CardVisual dealerCard = new CardVisual(dealerHand.get(i));
            CardVisual p1Card = new CardVisual(player1Hand.get(i));
            CardVisual p2Card = new CardVisual(player2Hand.get(i));

            dealerCard.setFaceDown();
            p1Card.setFaceDown();
            p2Card.setFaceDown();

            dealerCards.getChildren().add(dealerCard);
            player1Cards.getChildren().add(p1Card);
            player2Cards.getChildren().add(p2Card);
        }

        // Reset UI
        updateTotalWinningsLabelStyles();
        updatePushedAntesDisplay();
        resetUI();
    }

    private void resetUI() {
        // Clear text fields
        player1AnteField.clear();
        player1PairPlusField.clear();
        player2AnteField.clear();
        player2PairPlusField.clear();

        // Reset buttons
        player1PlayButton.setText("Play");
        player2PlayButton.setText("Play");
        player1FoldButton.setText("Fold");
        player2FoldButton.setText("Fold");

        // Enable controls
        setPlayer1ControlsEnabled(true);
        setPlayer2ControlsEnabled(true);

        // Reset game info
        gameInfoLabel.setText("Players: Place your ante bets");
        playAgainBox.setVisible(false);
        dealButton.setDisable(true);
    }

    private void revealDealerCards() {
        for (javafx.scene.Node node : dealerCards.getChildren()) {
            if (node instanceof CardVisual) {
                ((CardVisual) node).setFaceUp();
            }
        }
    }

    private void evaluateRound() {
        currentState = GameState.ROUND_COMPLETE;
        StringBuilder finalResult = new StringBuilder("Results:\n\n");

        // Calculate Player 1 results
        String player1Result = ThreeCardLogic.determineWinner(dealerHand, player1Hand,
                playerOne.anteBet, playerOne.playBet);
        int player1PairPlus = ThreeCardLogic.evalPPWinnings(player1Hand, playerOne.pairPlusBet);

        // Calculate Player 2 results
        String player2Result = ThreeCardLogic.determineWinner(dealerHand, player2Hand,
                playerTwo.anteBet, playerTwo.playBet);
        int player2PairPlus = ThreeCardLogic.evalPPWinnings(player2Hand, playerTwo.pairPlusBet);

        // Handle Player 1 results
        finalResult.append("Player 1:\n");
        finalResult.append(player1Result).append("\n");

        if (player1Result.contains("You win")) {
            // Player wins - they get their ante and play bet back plus matching amounts
            int winnings = (playerOne.anteBet + playerOne.playBet);
            playerOne.totalWinnings += winnings;
            // If there were pushed antes, they win those too
            if (player1PushedAntes > 0) {
                playerOne.totalWinnings += player1PushedAntes;
                finalResult.append("Won pushed antes: $").append(player1PushedAntes).append("\n");
                player1PushedAntes = 0;  // Clear pushed antes after winning
            }
            finalResult.append("Main Game Winnings: $").append(winnings).append("\n");
        } else if (player1Result.contains("Dealer wins")) {
            // Player loses current bets and pushed antes
            finalResult.append("Loss: $").append(playerOne.anteBet + playerOne.playBet).append("\n");
            if (player1PushedAntes > 0) {
                finalResult.append("Lost pushed antes: $").append(player1PushedAntes).append("\n");
                player1PushedAntes = 0;
            }
        } else if (player1Result.contains("Dealer does not qualify")) {
            // Return play bet, push ante
            finalResult.append("Play bet returned: $").append(playerOne.playBet).append("\n");
            player1PushedAntes += playerOne.anteBet;
            finalResult.append("Ante pushes to next hand (Total pushed: $")
                    .append(player1PushedAntes).append(")\n");
            playerOne.playBet = 0;
        }

        // Handle Pair Plus bet for Player 1
        if (player1PairPlus > 0) {
            playerOne.totalWinnings += player1PairPlus;
            finalResult.append("Pair Plus Winnings: $").append(player1PairPlus).append("\n");
        } else if (playerOne.pairPlusBet > 0) {
            finalResult.append("Pair Plus Loss: $").append(playerOne.pairPlusBet).append("\n");
        }

        // Handle Player 2 results
        finalResult.append("\nPlayer 2:\n");
        finalResult.append(player2Result).append("\n");

        if (player2Result.contains("You win")) {
            int winnings = (playerTwo.anteBet + playerTwo.playBet);
            playerTwo.totalWinnings += winnings;
            if (player2PushedAntes > 0) {
                playerTwo.totalWinnings += player2PushedAntes;
                finalResult.append("Won pushed antes: $").append(player2PushedAntes).append("\n");
                player2PushedAntes = 0;
            }
            finalResult.append("Main Game Winnings: $").append(winnings).append("\n");
        } else if (player2Result.contains("Dealer wins")) {
            finalResult.append("Loss: $").append(playerTwo.anteBet + playerTwo.playBet).append("\n");
            if (player2PushedAntes > 0) {
                finalResult.append("Lost pushed antes: $").append(player2PushedAntes).append("\n");
                player2PushedAntes = 0;
            }
        } else if (player2Result.contains("Dealer does not qualify")) {
            finalResult.append("Play bet returned: $").append(playerTwo.playBet).append("\n");
            player2PushedAntes += playerTwo.anteBet;
            finalResult.append("Ante pushes to next hand (Total pushed: $")
                    .append(player2PushedAntes).append(")\n");
            playerTwo.playBet = 0;
        }

        // Handle Pair Plus bet for Player 2
        if (player2PairPlus > 0) {
            playerTwo.totalWinnings += player2PairPlus;
            finalResult.append("Pair Plus Winnings: $").append(player2PairPlus).append("\n");
        } else if (playerTwo.pairPlusBet > 0) {
            finalResult.append("Pair Plus Loss: $").append(playerTwo.pairPlusBet).append("\n");
        }

        // Update the pushed antes labels
        updatePushedAntesDisplay();

        // Update displays
        updateWinningsDisplays();
        gameInfoLabel.setText(finalResult.toString());
        playAgainBox.setVisible(true);
        dealButton.setDisable(false);
    }

    // Update the updateWinningsDisplays method to only show positive numbers
    private void updateWinningsDisplays() {
        player1TotalWinningsLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-size: 14px; -fx-font-weight: bold;");
        player2TotalWinningsLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-size: 14px; -fx-font-weight: bold;");

        // Only display the positive winnings
        player1TotalWinningsLabel.setText(String.format("Total Winnings: $%d", Math.max(0, playerOne.totalWinnings)));
        player2TotalWinningsLabel.setText(String.format("Total Winnings: $%d", Math.max(0, playerTwo.totalWinnings)));

        // Remove these lines as the labels are already added in createGameScreen()
        // player1Area.getChildren().add(1, player1PushedAntesLabel);
        // player2Area.getChildren().add(1, player2PushedAntesLabel);
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

    private void showGameScreen() {
        primaryStage.setScene(gameScene);
        primaryStage.setTitle("Three Card Poker - Game");
        startNewRound();
        // Ensure menu stays visible by re-applying styles if needed
        if (gameScene.getStylesheets().isEmpty()) {
            String css = getClass().getResource("/css/dark-theme.css").toExternalForm();
            gameScene.getStylesheets().add(css);
        }
    }

    private void showStartScreen() {
        primaryStage.setScene(startScene);
        primaryStage.setTitle("Three Card Poker");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
