
import java.util.ArrayList;
import java.util.Arrays;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
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
    private VBox dealerArea;

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

        // Create buttons
        Button startButton = new Button("Start Game");
        startButton.getStyleClass().add("primary-button");

        Button rulesButton = new Button("Game Rules");  // Add rules button
        rulesButton.getStyleClass().add("rules-button");

        Button exitButton = new Button("Exit");
        exitButton.getStyleClass().add("danger-button");

        // Add button actions
        startButton.setOnAction(e -> {
            showGameScreen();
            startNewRound();
        });
        rulesButton.setOnAction(e -> showRulesDialog());  // Add rules action
        exitButton.setOnAction(e -> Platform.exit());

        startRoot.getChildren().addAll(titleText, startButton, rulesButton, exitButton);
        startScene = new Scene(startRoot, 1200, 800);
    }

    private StackPane createCardNode(Card card, boolean isFaceDown) {
        StackPane cardPane = new StackPane();
        cardPane.setPrefSize(100, 140);
        cardPane.setAlignment(Pos.CENTER);

        // Create card background/outline
        Rectangle background = new Rectangle(100, 140);
        background.setArcWidth(10);
        background.setArcHeight(10);

        // Create front content
        VBox frontContent = new VBox(5);
        frontContent.setAlignment(Pos.CENTER);

        // Top value and suit
        Text valueText = new Text(getCardValueString(card));
        Text suitText = new Text(getSuitSymbol(card));
        valueText.setFont(Font.font("Arial", 20));
        suitText.setFont(Font.font("Arial", 20));

        // Set color based on suit
        Color cardColor = (card.getSuit() == 'H' || card.getSuit() == 'D') ? Color.RED : Color.BLACK;
        valueText.setFill(cardColor);
        suitText.setFill(cardColor);

        // Center suit
        Text centerSuit = new Text(getSuitSymbol(card));
        centerSuit.setFont(Font.font("Arial", 40));
        centerSuit.setFill(cardColor);

        frontContent.getChildren().addAll(valueText, centerSuit, suitText);

        // Add all elements
        cardPane.getChildren().addAll(background, frontContent);

        // Set initial state
        if (isFaceDown) {
            frontContent.setVisible(false);
            background.setFill(Color.TRANSPARENT);
            background.setStroke(Color.WHITE);     // Make outline white for visibility
            background.setStrokeWidth(3);          // Make outline thicker
            background.getStrokeDashArray().addAll(5d, 5d); // Add dashed effect
        } else {
            frontContent.setVisible(true);
            background.setFill(Color.WHITE);
            background.setStroke(Color.BLACK);
            background.setStrokeWidth(1);
            background.getStrokeDashArray().clear(); // Remove dash effect for face-up cards
        }

        // Add methods to flip card
        cardPane.setUserData(new Object[]{frontContent, background, card});

        return cardPane;
    }

    private void setCardFaceUp(StackPane cardPane) {
        Object[] data = (Object[]) cardPane.getUserData();
        VBox frontContent = (VBox) data[0];
        Rectangle background = (Rectangle) data[1];

        frontContent.setVisible(true);
        background.setFill(Color.WHITE);
        background.setStroke(Color.BLACK);
        background.setStrokeWidth(1);
        background.getStrokeDashArray().clear();
    }

    private String getCardValueString(Card card) {
        switch (card.getValue()) {
            case 14:
                return "A";
            case 13:
                return "K";
            case 12:
                return "Q";
            case 11:
                return "J";
            default:
                return String.valueOf(card.getValue());
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
        dealerArea = new VBox(10);  // Create as class field now
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
        menuBar.setUseSystemMenuBar(false);

        Menu optionsMenu = new Menu("Options");
        optionsMenu.getStyleClass().add("menu");

        MenuItem exitItem = new MenuItem("Exit");
        MenuItem freshStartItem = new MenuItem("Fresh Start");
        MenuItem rulesItem = new MenuItem("Game Rules");  // Add rules menu item
        newLookItem = new MenuItem("Switch to Light Theme");

        // Add styles to menu items
        Arrays.asList(exitItem, freshStartItem, rulesItem, newLookItem)
                .forEach(item -> item.getStyleClass().add("menu-item"));

        // Add event handlers
        exitItem.setOnAction(e -> showExitScreen());
        freshStartItem.setOnAction(e -> resetGame());
        rulesItem.setOnAction(e -> showRulesDialog());  // Add rules action
        newLookItem.setOnAction(e -> toggleTheme());

        optionsMenu.getItems().addAll(exitItem, freshStartItem, rulesItem, newLookItem);
        menuBar.getMenus().add(optionsMenu);

        // Add MenuBar to top of BorderPane
        VBox topContainer = new VBox();
        topContainer.setSpacing(0);  // Set spacing to 0
        topContainer.getChildren().addAll(menuBar, playAgainBox);
        gameRoot.setTop(topContainer);
    }

    private void updateTotalWinningsLabelStyles() {
        player1TotalWinningsLabel.getStyleClass().add("winnings-label");
        player2TotalWinningsLabel.getStyleClass().add("winnings-label");
    }

    private void showExitScreen() {
        if (exitScene == null) {
            createExitScreen();
        }
        primaryStage.setScene(exitScene);
    }

    private void createExitScreen() {
        VBox exitRoot = new VBox();
        exitRoot.getStyleClass().add("exit-root");

        // Title
        Text exitText = new Text("Are you sure you want to exit?");
        exitText.getStyleClass().add("exit-text");

        // Subtitle with current standings
        Text standingsText = new Text(String.format(
                "Current Standings:\nPlayer 1: $%d\nPlayer 2: $%d",
                playerOne.totalWinnings,
                playerTwo.totalWinnings
        ));
        standingsText.getStyleClass().add("standings-text");

        // Buttons container
        HBox buttonBox = new HBox();
        buttonBox.getStyleClass().add("button-box");

        // Continue button
        Button continueButton = new Button("Continue Playing");
        continueButton.getStyleClass().add("continue-button");
        continueButton.setOnAction(e -> returnToGame());

        // Quit button
        Button quitButton = new Button("Quit Game");
        quitButton.getStyleClass().add("quit-button");
        quitButton.setOnAction(e -> Platform.exit());

        // Add buttons to button container
        buttonBox.getChildren().addAll(continueButton, quitButton);

        // Add all elements to root container
        exitRoot.getChildren().addAll(exitText, standingsText, buttonBox);

        // Create scene
        exitScene = new Scene(exitRoot, 500, 300);

        // Add the same stylesheet as the main game
        exitScene.getStylesheets().addAll(gameScene.getStylesheets());
    }

    private void returnToGame() {
        primaryStage.setScene(gameScene);
    }

// Add this new method to update the pushed antes displays:
    private void updatePushedAntesDisplay() {
        player1PushedAntesLabel.setText(String.format("Pushed Antes: $%d", player1PushedAntes));
        player2PushedAntesLabel.setText(String.format("Pushed Antes: $%d", player2PushedAntes));
    }

    private void resetGame() {
        // Reset player values
        playerOne.totalWinnings = 0;
        playerTwo.totalWinnings = 0;
        player1PushedAntes = 0;
        player2PushedAntes = 0;

        // Reset all flags
        player1InitialBetMade = false;
        player2InitialBetMade = false;
        player1PlayDecisionMade = false;
        player2PlayDecisionMade = false;

        // Clear all text fields
        player1AnteField.clear();
        player1PairPlusField.clear();
        player2AnteField.clear();
        player2PairPlusField.clear();

        // Reset buttons
        player1PlayButton.setText("Play");
        player2PlayButton.setText("Play");
        player1FoldButton.setText("Fold");
        player2FoldButton.setText("Fold");

        // Enable all controls
        setPlayer1ControlsEnabled(true);
        setPlayer2ControlsEnabled(true);

        // Clear game info
        gameInfoLabel.setText("Players: Place your bets");

        // Clear previous results if any
        dealerArea.getChildren().removeIf(node
                -> node instanceof HBox
                && ((HBox) node).getStyleClass().contains("results-container")
        );

        // Reset displays
        updateWinningsDisplays();
        updatePushedAntesDisplay();

        // Hide play again box
        playAgainBox.setVisible(false);

        // Start new round
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

                // Only reveal Player 1's cards after bet is made
                for (javafx.scene.Node node : player1Cards.getChildren()) {
                    if (node instanceof StackPane) {
                        setCardFaceUp((StackPane) node);
                    }
                }

                checkInitialBets();

            } catch (NumberFormatException e) {
                gameInfoLabel.setText("Please enter valid bets");
                return;
            }
        } else {
            // Rest of the play/fold logic remains the same
            if (isPlay) {
                playerOne.playBet = playerOne.anteBet;
            } else {
                playerOne.anteBet = 0;
                playerOne.playBet = 0;
                playerOne.pairPlusBet = 0;
            }

            player1PlayDecisionMade = true;
            player1PlayButton.setDisable(true);
            player1FoldButton.setDisable(true);
            player1PlayButton.setText(isPlay ? "Play Bet Made" : "Folded");
            player1FoldButton.setText(isPlay ? "Play Bet Made" : "Folded");

            checkPlayDecisions();
        }
    }

    private void showRulesDialog() {
        Stage rulesStage = new Stage();
        rulesStage.initModality(Modality.APPLICATION_MODAL);
        rulesStage.setTitle("Three Card Poker Rules");

        VBox rulesContent = new VBox();
        rulesContent.getStyleClass().add("rules-content");

        // Title
        Label titleLabel = new Label("Three Card Poker Rules");
        titleLabel.getStyleClass().add("rules-dialog-title");

        // Rules text
        VBox rulesText = new VBox(10);
        rulesText.getChildren().addAll(
                createRuleSection("Basic Play:",
                        "• Players make an Ante bet to compete against the dealer\n"
                        + "• Ante bets must be between $5 and $25\n"
                        + "• Players may also make an optional Pair Plus bet ($5-$25)\n"
                        + "• Three cards are dealt to each player and the dealer\n"
                        + "• Players must then either fold or make a Play bet equal to their Ante"),
                createRuleSection("Dealer Qualification:",
                        "• Dealer must have Queen-high or better to qualify\n"
                        + "• If dealer doesn't qualify:\n"
                        + "  - Play bet is returned\n"
                        + "  - Ante bet is pushed to next hand\n"
                        + "• If dealer qualifies, highest poker hand wins"),
                createRuleSection("Hand Rankings (Highest to Lowest):",
                        "1. Straight Flush\n"
                        + "2. Three of a Kind\n"
                        + "3. Straight\n"
                        + "4. Flush\n"
                        + "5. Pair\n"
                        + "6. High Card"),
                createRuleSection("Pair Plus Payouts:",
                        "• Straight Flush: 40 to 1\n"
                        + "• Three of a Kind: 30 to 1\n"
                        + "• Straight: 6 to 1\n"
                        + "• Flush: 3 to 1\n"
                        + "• Pair: 1 to 1"),
                createRuleSection("Important Notes:",
                        "• Pair Plus bet wins regardless of dealer's hand\n"
                        + "• Ante and Play bets pay 1 to 1 on wins\n"
                        + "• Folding forfeits both Ante and Pair Plus bets\n"
                        + "• Ace can be high or low in straights")
        );

        ScrollPane scrollPane = new ScrollPane(rulesText);
        scrollPane.getStyleClass().add("rules-scroll-pane");

        // Close button
        Button closeButton = new Button("Close");
        closeButton.getStyleClass().add("primary-button");
        closeButton.setOnAction(e -> rulesStage.close());

        HBox buttonBox = new HBox(closeButton);
        buttonBox.getStyleClass().add("rules-button-box");

        rulesContent.getChildren().addAll(titleLabel, scrollPane, buttonBox);

        Scene rulesScene = new Scene(rulesContent, 500, 600);
        rulesScene.getStylesheets().addAll(gameScene.getStylesheets());

        rulesStage.setScene(rulesScene);
        rulesStage.show();
    }

    private VBox createRuleSection(String title, String content) {
        VBox section = new VBox();
        section.getStyleClass().add("rule-section");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("rule-title");

        Label contentLabel = new Label(content);

        section.getChildren().addAll(titleLabel, contentLabel);
        return section;
    }

    private void createRulesButton() {
        Button rulesButton = new Button("Game Rules");
        rulesButton.getStyleClass().add("rules-button");
        rulesButton.setOnAction(e -> showRulesDialog());

        // Add to top bar
        VBox topBar = (VBox) ((BorderPane) gameScene.getRoot()).getTop();
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER);

        // Get existing back button
        Button backButton = (Button) topBar.getChildren().get(0);
        buttonContainer.getChildren().addAll(backButton, rulesButton);

        // Update topBar children
        topBar.getChildren().set(0, buttonContainer);
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

                // Only reveal Player 2's cards after bet is made
                for (javafx.scene.Node node : player2Cards.getChildren()) {
                    if (node instanceof StackPane) {
                        setCardFaceUp((StackPane) node);
                    }
                }

                checkInitialBets();

            } catch (NumberFormatException e) {
                gameInfoLabel.setText("Please enter valid bets");
                return;
            }
        } else {
            // Rest of the play/fold logic remains the same
            if (isPlay) {
                playerTwo.playBet = playerTwo.anteBet;
            } else {
                playerTwo.anteBet = 0;
                playerTwo.playBet = 0;
                playerTwo.pairPlusBet = 0;
            }

            player2PlayDecisionMade = true;
            player2PlayButton.setDisable(true);
            player2FoldButton.setDisable(true);
            player2PlayButton.setText(isPlay ? "Play Bet Made" : "Folded");
            player2FoldButton.setText(isPlay ? "Play Bet Made" : "Folded");

            checkPlayDecisions();
        }
    }

    private void startNewRound() {
        // Reset all betting flags
        player1InitialBetMade = false;
        player2InitialBetMade = false;
        player1PlayDecisionMade = false;
        player2PlayDecisionMade = false;

        // Deal cards
        dealerHand = theDealer.dealHand();
        player1Hand = theDealer.dealHand();
        player2Hand = theDealer.dealHand();

        // Clear and reset displays
        dealerCards.getChildren().clear();
        player1Cards.getChildren().clear();
        player2Cards.getChildren().clear();

        // Add cards face down initially
        for (int i = 0; i < 3; i++) {
            StackPane dealerCard = createCardNode(dealerHand.get(i), true);
            StackPane p1Card = createCardNode(player1Hand.get(i), true); // Start face down
            StackPane p2Card = createCardNode(player2Hand.get(i), true); // Start face down

            dealerCards.getChildren().add(dealerCard);
            player1Cards.getChildren().add(p1Card);
            player2Cards.getChildren().add(p2Card);
        }

        // Reset UI
        updateTotalWinningsLabelStyles();
        updatePushedAntesDisplay();
        resetUI();
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
            if (node instanceof StackPane) {
                setCardFaceUp((StackPane) node);
            }
        }
    }

    private void evaluateRound() {
        currentState = GameState.ROUND_COMPLETE;

        // First evaluate Pair Plus for both players (this should happen regardless of dealer qualification)
        int player1PairPlus = ThreeCardLogic.evalPPWinnings(player1Hand, playerOne.pairPlusBet);
        int player2PairPlus = ThreeCardLogic.evalPPWinnings(player2Hand, playerTwo.pairPlusBet);

        // Add Pair Plus winnings immediately
        playerOne.totalWinnings += player1PairPlus;
        playerTwo.totalWinnings += player2PairPlus;

        // Calculate main game results
        String player1Result = ThreeCardLogic.determineWinner(dealerHand, player1Hand,
                playerOne.anteBet, playerOne.playBet);
        String player2Result = ThreeCardLogic.determineWinner(dealerHand, player2Hand,
                playerTwo.anteBet, playerTwo.playBet);

        // Create containers for side-by-side display
        HBox resultsContainer = new HBox(40);
        resultsContainer.setAlignment(Pos.CENTER);
        resultsContainer.getStyleClass().add("results-container");

        VBox player1Results = new VBox(5);
        VBox player2Results = new VBox(5);

        // Build Player 1 results
        StringBuilder p1Result = new StringBuilder();
        StringBuilder p2Result = new StringBuilder();

        // Add pair plus results first
        p1Result.append("Player 1:\n");
        if (player1PairPlus > 0) {
            p1Result.append("Pair Plus Winnings: $").append(player1PairPlus).append("\n");
        }
        p1Result.append(player1Result).append("\n");

        p2Result.append("Player 2:\n");
        if (player2PairPlus > 0) {
            p2Result.append("Pair Plus Winnings: $").append(player2PairPlus).append("\n");
        }
        p2Result.append(player2Result).append("\n");

        // Handle main game results
        if (player1Result.contains("Dealer does not qualify")) {
            p1Result.append("Play bet returned: $").append(playerOne.playBet).append("\n");
            player1PushedAntes += playerOne.anteBet;
            p1Result.append("Ante pushes to next hand (Total pushed: $")
                    .append(player1PushedAntes).append(")\n");
            playerOne.playBet = 0;
        } else if (player1Result.contains("It's a tie")) {
            p1Result.append("Tie - bets returned\n");
        } else if (player1Result.contains("You win")) {
            int mainGameWinnings = (playerOne.anteBet + playerOne.playBet);
            playerOne.totalWinnings += mainGameWinnings;
            if (player1PushedAntes > 0) {
                playerOne.totalWinnings += player1PushedAntes;
                p1Result.append("Won pushed antes: $").append(player1PushedAntes).append("\n");
                player1PushedAntes = 0;
            }
            p1Result.append("Main Game Winnings: $").append(mainGameWinnings).append("\n");
        } else if (player1Result.contains("Dealer wins")) {
            p1Result.append("Main Game Loss\n");
            if (player1PushedAntes > 0) {
                p1Result.append("Lost pushed antes: $").append(player1PushedAntes).append("\n");
                player1PushedAntes = 0;
            }
        }

        // Handle Player 2 results similarly
        if (player2Result.contains("Dealer does not qualify")) {
            p2Result.append("Play bet returned: $").append(playerTwo.playBet).append("\n");
            player2PushedAntes += playerTwo.anteBet;
            p2Result.append("Ante pushes to next hand (Total pushed: $")
                    .append(player2PushedAntes).append(")\n");
            playerTwo.playBet = 0;
        } else if (player2Result.contains("It's a tie")) {
            p2Result.append("Tie - bets returned\n");
        } else if (player2Result.contains("You win")) {
            int mainGameWinnings = (playerTwo.anteBet + playerTwo.playBet);
            playerTwo.totalWinnings += mainGameWinnings;
            if (player2PushedAntes > 0) {
                playerTwo.totalWinnings += player2PushedAntes;
                p2Result.append("Won pushed antes: $").append(player2PushedAntes).append("\n");
                player2PushedAntes = 0;
            }
            p2Result.append("Main Game Winnings: $").append(mainGameWinnings).append("\n");
        } else if (player2Result.contains("Dealer wins")) {
            p2Result.append("Main Game Loss\n");
            if (player2PushedAntes > 0) {
                p2Result.append("Lost pushed antes: $").append(player2PushedAntes).append("\n");
                player2PushedAntes = 0;
            }
        }

        // Create and style labels for results
        Label player1ResultLabel = new Label(p1Result.toString());
        Label player2ResultLabel = new Label(p2Result.toString());

        player1ResultLabel.getStyleClass().add("game-info-text");
        player2ResultLabel.getStyleClass().add("game-info-text");

        // Add results to their containers
        player1Results.getChildren().add(player1ResultLabel);
        player2Results.getChildren().add(player2ResultLabel);

        resultsContainer.getChildren().addAll(player1Results, player2Results);

        // Update displays
        gameInfoLabel.setText("Results:");

        // Clear previous results if any
        dealerArea.getChildren().removeIf(node -> node instanceof HBox
                && ((HBox) node).getStyleClass().contains("results-container"));

        // Add new results
        dealerArea.getChildren().add(resultsContainer);

        // Update other UI elements
        updatePushedAntesDisplay();
        updateWinningsDisplays();
        playAgainBox.setVisible(true);
        dealButton.setDisable(false);
    }

    // Update the updateWinningsDisplays method to only show positive numbers
    private void updateWinningsDisplays() {
        // Ensure labels have the correct style class
        player1TotalWinningsLabel.getStyleClass().add("winnings-label");
        player2TotalWinningsLabel.getStyleClass().add("winnings-label");

        // Only display the positive winnings
        player1TotalWinningsLabel.setText(String.format("Total Winnings: $%d",
                Math.max(0, playerOne.totalWinnings)));
        player2TotalWinningsLabel.setText(String.format("Total Winnings: $%d",
                Math.max(0, playerTwo.totalWinnings)));
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
