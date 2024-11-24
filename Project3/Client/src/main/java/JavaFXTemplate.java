
import java.util.ArrayList;
import java.util.Collections;

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
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class JavaFXTemplate extends Application {

    // Constants
    private static final double SCENE_WIDTH = 1200;
    private static final double SCENE_HEIGHT = 800;
    private static final double CARD_WIDTH = 100;
    private static final double CARD_HEIGHT = 140;

    // Enums
    private enum GameState {
        PLAYER_ONE_TURN, PLAYER_TWO_TURN, ROUND_COMPLETE
    }

    // Game components
    private final Player playerOne = new Player();
    private final Player playerTwo = new Player();
    private final Dealer theDealer = new Dealer();
    private GameState currentState;
    private String currentTheme = "default";

    // Hands
    private ArrayList<Card> dealerHand;
    private ArrayList<Card> player1Hand;
    private ArrayList<Card> player2Hand;

    // UI Stage and Scenes
    private Stage primaryStage;
    private Scene startScene;
    private Scene gameScene;
    private Scene exitScene;

    // UI Components - Dealer
    private Button dealButton;
    private HBox dealerCards;
    private VBox dealerArea;

    // UI Components - Player 1
    private TextField player1AnteField;
    private TextField player1PairPlusField;
    private Button player1PlayButton;
    private Button player1FoldButton;
    private VBox player1Area;
    private HBox player1Cards;
    private Label player1TotalWinningsLabel;
    private Label player1PushedAntesLabel;
    private int player1PushedAntes = 0;

    // UI Components - Player 2
    private TextField player2AnteField;
    private TextField player2PairPlusField;
    private Button player2PlayButton;
    private Button player2FoldButton;
    private VBox player2Area;
    private HBox player2Cards;
    private Label player2TotalWinningsLabel;
    private Label player2PushedAntesLabel;
    private int player2PushedAntes = 0;

    // Additional UI
    private HBox playAgainBox;
    private Label gameInfoLabel;
    private MenuItem newLookItem;
    private boolean player1InitialBetMade = false;
    private boolean player2InitialBetMade = false;
    private boolean player1PlayDecisionMade = false;
    private boolean player2PlayDecisionMade = false;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Three Card Poker");

        initializeTheme();
        createScenes();

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

    private VBox createRuleSection(String title, String content) {
        VBox section = new VBox();
        section.getStyleClass().add("rule-section");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("rule-title");

        Label contentLabel = new Label(content);

        section.getChildren().addAll(titleLabel, contentLabel);
        return section;
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
                createRuleSection("Basic Play:", "• Players make an Ante bet to compete against the dealer\n" + "• Ante bets must be between $5 and $25\n" + "• Players may also make an optional Pair Plus bet ($5-$25)\n" + "• Three cards are dealt to each player and the dealer\n" + "• Players must then either fold or make a Play bet equal to their Ante"),
                createRuleSection("Dealer Qualification:", "• Dealer must have Queen-high or better to qualify\n" + "• If dealer doesn't qualify:\n" + "  - Play bet is returned\n" + "  - Ante bet is pushed to next hand\n" + "• If dealer qualifies, highest poker hand wins"),
                createRuleSection("Hand Rankings (Highest to Lowest):", "1. Straight Flush\n" + "2. Three of a Kind\n" + "3. Straight\n" + "4. Flush\n" + "5. Pair\n" + "6. High Card"),
                createRuleSection("Pair Plus Payouts:", "• Straight Flush: 40 to 1\n" + "• Three of a Kind: 30 to 1\n" + "• Straight: 6 to 1\n" + "• Flush: 3 to 1\n" + "• Pair: 1 to 1"),
                createRuleSection("Important Notes:", "• Pair Plus bet wins regardless of dealer's hand\n" + "• Ante and Play bets pay 1 to 1 on wins\n" + "• Folding forfeits both Ante and Pair Plus bets\n" + "• Ace can be high or low in straights")
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

    private void initializeTheme() {
        String css = getClass().getResource("/css/dark-theme.css").toExternalForm();
        currentTheme = "default";
        Platform.runLater(() -> {
            startScene.getStylesheets().add(css);
            gameScene.getStylesheets().add(css);
        });
    }

    private void createScenes() {
        createStartScreen();
        createGameScreen();
    }

    private void createStartScreen() {
        VBox startRoot = new VBox(20);
        startRoot.setAlignment(Pos.CENTER);
        startRoot.getStyleClass().add("root");

        Text titleText = createStyledText("Welcome to Three Card Poker", "title-text");

        Button[] buttons = createStartButtons();
        startRoot.getChildren().addAll(titleText);
        startRoot.getChildren().addAll(buttons);

        startScene = new Scene(startRoot, SCENE_WIDTH, SCENE_HEIGHT);
    }

    private Button[] createStartButtons() {
        Button startButton = createStyledButton("Start Game", "primary-button",
                e -> {
                    showGameScreen();
                    startNewRound();
                });

        Button rulesButton = createStyledButton("Game Rules", "rules-button",
                e -> showRulesDialog());

        Button exitButton = createStyledButton("Exit", "danger-button",
                e -> Platform.exit());

        return new Button[]{startButton, rulesButton, exitButton};
    }

    private StackPane createCardNode(Card card, boolean isFaceDown) {
        // Create visual representation using CardVisual class
        CardVisual cardVisual = new CardVisual(card);

        // Set face up/down state
        if (isFaceDown) {
            cardVisual.setFaceDown();
        } else {
            cardVisual.setFaceUp();
        }

        return cardVisual;  // CardVisual already extends StackPane
    }

    private Button createStyledButton(String text, String styleClass, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button button = new Button(text);
        button.getStyleClass().add(styleClass);
        button.setOnAction(handler);
        return button;
    }

    private Text createStyledText(String content, String styleClass) {
        Text text = new Text(content);
        text.getStyleClass().add(styleClass);
        return text;
    }

    private void createGameScreen() {
        BorderPane gameRoot = new BorderPane();
        gameRoot.getStyleClass().add("root");

        // Create and set up main components
        ScrollPane scrollPane = createScrollPane();
        VBox mainContent = createMainContent();
        MenuBar menuBar = createMenuBar();
        VBox topContainer = createTopContainer(menuBar);

        // Set up scroll pane and root
        scrollPane.setContent(mainContent);
        gameRoot.setTop(topContainer);
        gameRoot.setCenter(scrollPane);

        gameScene = new Scene(gameRoot, 1200, 800);
    }

    private ScrollPane createScrollPane() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    private VBox createMainContent() {
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.getStyleClass().add("root");

        dealerArea = createDealerArea();
        HBox playersContainer = createPlayersContainer();

        mainContent.getChildren().addAll(dealerArea, playersContainer);
        return mainContent;
    }

    private VBox createDealerArea() {
        VBox area = new VBox(10);
        area.setAlignment(Pos.CENTER);

        Label dealerLabel = createStyledLabel("Dealer's Cards", "dealer-label");
        dealerCards = createCardContainer();
        gameInfoLabel = createStyledLabel("Players: Place your bets", "game-info-label");

        area.getChildren().addAll(dealerLabel, dealerCards, gameInfoLabel);
        return area;
    }

    private HBox createPlayersContainer() {
        HBox container = new HBox(50);
        container.setAlignment(Pos.CENTER);

        player1Area = createPlayerArea(1);
        player2Area = createPlayerArea(2);

        container.getChildren().addAll(player1Area, player2Area);
        return container;
    }

    private TextField createStyledTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("styled-textfield");
        return field;
    }

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

    private VBox createPlayerArea(int playerNum) {
        VBox area = new VBox(10);
        area.getStyleClass().add("player-area");

        // Create components based on player number
        boolean isPlayer1 = playerNum == 1;
        Label playerLabel = createStyledLabel("Player " + playerNum + " Cards", "player-label");
        HBox cardContainer = createCardContainer();

        // Set appropriate class fields
        if (isPlayer1) {
            player1Cards = cardContainer;
            player1AnteField = createStyledTextField("Enter Ante Bet");
            player1PairPlusField = createStyledTextField("Enter Pair Plus Bet");
            player1PlayButton = createStyledButton("Play", "#4CAF50");
            player1FoldButton = createStyledButton("Fold", "#f44336");
            player1TotalWinningsLabel = createStyledLabel("Total Winnings: $0", "winnings-label");
            player1PushedAntesLabel = createStyledLabel("Pushed Antes: $0", "pushed-antes-label");
        } else {
            player2Cards = cardContainer;
            player2AnteField = createStyledTextField("Enter Ante Bet");
            player2PairPlusField = createStyledTextField("Enter Pair Plus Bet");
            player2PlayButton = createStyledButton("Play", "#4CAF50");
            player2FoldButton = createStyledButton("Fold", "#f44336");
            player2TotalWinningsLabel = createStyledLabel("Total Winnings: $0", "winnings-label");
            player2PushedAntesLabel = createStyledLabel("Pushed Antes: $0", "pushed-antes-label");
        }

        HBox buttonContainer = createPlayerButtonContainer(isPlayer1);
        setupPlayerButtons(isPlayer1);

        // Add all components to player area
        area.getChildren().addAll(
                playerLabel,
                isPlayer1 ? player1TotalWinningsLabel : player2TotalWinningsLabel,
                isPlayer1 ? player1PushedAntesLabel : player2PushedAntesLabel,
                cardContainer,
                createBetFields(isPlayer1),
                buttonContainer
        );

        return area;
    }

    private HBox createCardContainer() {
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER);
        container.getStyleClass().add("card-container");
        return container;
    }

    private VBox createBetFields(boolean isPlayer1) {
        VBox betContainer = new VBox(5);

        HBox anteBox = new HBox(5);
        HBox pairPlusBox = new HBox(5);

        Label anteLabel = createStyledLabel("Ante:", "player-label");
        Label pairPlusLabel = createStyledLabel("Pair+:", "player-label");

        anteBox.getChildren().addAll(anteLabel,
                isPlayer1 ? player1AnteField : player2AnteField);
        pairPlusBox.getChildren().addAll(pairPlusLabel,
                isPlayer1 ? player1PairPlusField : player2PairPlusField);

        betContainer.getChildren().addAll(anteBox, pairPlusBox);
        return betContainer;
    }

    private HBox createPlayerButtonContainer(boolean isPlayer1) {
        HBox container = new HBox(10);
        container.getStyleClass().add("button-container");
        container.getChildren().addAll(
                isPlayer1 ? player1PlayButton : player2PlayButton,
                isPlayer1 ? player1FoldButton : player2FoldButton
        );
        return container;
    }

    private VBox createTopContainer(MenuBar menuBar) {
        VBox topContainer = new VBox(0);  // spacing = 0

        // Create back button and play again box
        Button backButton = createBackButton();
        createPlayAgainBox();  // This creates and sets up the playAgainBox field

        // Add components to top container
        topContainer.getChildren().addAll(menuBar, backButton, playAgainBox);
        return topContainer;
    }

    private Button createBackButton() {
        Button backButton = new Button("Back to Menu");
        backButton.getStyleClass().add("danger-button");
        backButton.setOnAction(e -> showStartScreen());
        return backButton;
    }

    private void createPlayAgainBox() {
        playAgainBox = new HBox(10);
        playAgainBox.setAlignment(Pos.CENTER);

        Label playAgainLabel = createStyledLabel("Play Again?", "player-label");
        dealButton = createDealButton();

        playAgainBox.getChildren().addAll(playAgainLabel, dealButton);
        playAgainBox.setVisible(false);
    }

    private Button createDealButton() {
        Button button = new Button("Deal");
        button.getStyleClass().add("primary-button");
        button.setOnAction(e -> {
            playAgainBox.setVisible(false);
            startNewRound();
        });
        return button;
    }

    private void checkInitialBets() {
        if (player1InitialBetMade && player2InitialBetMade) {
            gameInfoLabel.setText("Players: Review your cards and make play bet or fold");
        }
    }

    private String determineWinner(ArrayList<Card> dealer, ArrayList<Card> player, int anteBet, int playBet) {
        // First check if dealer qualifies (Queen high or better)
        ArrayList<Integer> dealerValues = new ArrayList<>();
        for (Card card : dealer) {
            dealerValues.add(card.getValue());
        }
        Collections.sort(dealerValues, Collections.reverseOrder());

        // If dealer doesn't qualify (less than Queen high)
        if (dealerValues.get(0) < 12) {
            return "Dealer does not qualify (less than Queen high)";
        }

        // If dealer qualifies, compare hands using existing ThreeCardLogic method
        int result = ThreeCardLogic.compareHands(dealer, player);

        switch (result) {
            case 1:
                return "Dealer wins";
            case 2:
                return "You win";
            default:
                return "It's a tie";
        }
    }

    private void evaluateRound() {
        currentState = GameState.ROUND_COMPLETE;

        // First evaluate Pair Plus for both players
        int player1PairPlus = ThreeCardLogic.evalPPWinnings(player1Hand, playerOne.pairPlusBet);
        int player2PairPlus = ThreeCardLogic.evalPPWinnings(player2Hand, playerTwo.pairPlusBet);

        // Add Pair Plus winnings immediately
        playerOne.totalWinnings += player1PairPlus;
        playerTwo.totalWinnings += player2PairPlus;

        // Calculate main game results using our helper method
        String player1Result = determineWinner(dealerHand, player1Hand, playerOne.anteBet, playerOne.playBet);
        String player2Result = determineWinner(dealerHand, player2Hand, playerTwo.anteBet, playerTwo.playBet);

        // Create containers for side-by-side display
        HBox resultsContainer = new HBox(40);
        resultsContainer.setAlignment(Pos.CENTER);
        resultsContainer.getStyleClass().add("results-container");

        // Process results for both players
        VBox[] playerResults = {new VBox(5), new VBox(5)};
        StringBuilder[] results = {new StringBuilder(), new StringBuilder()};
        Player[] players = {playerOne, playerTwo};
        int[] pairPlusWinnings = {player1PairPlus, player2PairPlus};
        String[] gameResults = {player1Result, player2Result};

        for (int i = 0; i < 2; i++) {
            StringBuilder result = results[i];
            Player player = players[i];
            int pairPlus = pairPlusWinnings[i];
            String gameResult = gameResults[i];

            // Build result text
            result.append("Player ").append(i + 1).append(":\n");
            if (pairPlus > 0) {
                result.append("Pair Plus Winnings: $").append(pairPlus).append("\n");
            }
            result.append(gameResult).append("\n");

            // Handle main game results
            if (gameResult.contains("Dealer does not qualify")) {
                handleDealerNotQualified(result, player, i);
            } else if (gameResult.contains("It's a tie")) {
                result.append("Tie - bets returned\n");
            } else if (gameResult.contains("You win")) {
                handlePlayerWin(result, player, i);
            } else if (gameResult.contains("Dealer wins")) {
                handlePlayerLoss(result, player, i);
            }

            // Create and add result label
            Label resultLabel = new Label(result.toString());
            resultLabel.getStyleClass().add("game-info-text");
            playerResults[i].getChildren().add(resultLabel);
        }

        resultsContainer.getChildren().addAll(playerResults);

        // Update UI
        gameInfoLabel.setText("Results:");
        dealerArea.getChildren().removeIf(node
                -> node instanceof HBox && ((HBox) node).getStyleClass().contains("results-container")
        );
        dealerArea.getChildren().add(resultsContainer);

        // Final updates
        updatePushedAntesDisplay();
        updateWinningsDisplays();
        playAgainBox.setVisible(true);
        dealButton.setDisable(false);
    }

    private void handleDealerNotQualified(StringBuilder result, Player player, int playerIndex) {
        result.append("Play bet returned: $").append(player.playBet).append("\n");
        if (playerIndex == 0) {
            player1PushedAntes += player.anteBet;
            result.append("Ante pushes to next hand (Total pushed: $")
                    .append(player1PushedAntes).append(")\n");
        } else {
            player2PushedAntes += player.anteBet;
            result.append("Ante pushes to next hand (Total pushed: $")
                    .append(player2PushedAntes).append(")\n");
        }
        player.playBet = 0;
    }

    private void handlePlayerWin(StringBuilder result, Player player, int playerIndex) {
        int mainGameWinnings = (player.anteBet + player.playBet);
        player.totalWinnings += mainGameWinnings;

        int pushedAntes = playerIndex == 0 ? player1PushedAntes : player2PushedAntes;
        if (pushedAntes > 0) {
            player.totalWinnings += pushedAntes;
            result.append("Won pushed antes: $").append(pushedAntes).append("\n");
            if (playerIndex == 0) {
                player1PushedAntes = 0; 
            }else {
                player2PushedAntes = 0;
            }
        }
        result.append("Main Game Winnings: $").append(mainGameWinnings).append("\n");
    }

    private void handlePlayerLoss(StringBuilder result, Player player, int playerIndex) {
        result.append("Main Game Loss\n");
        int pushedAntes = playerIndex == 0 ? player1PushedAntes : player2PushedAntes;
        if (pushedAntes > 0) {
            result.append("Lost pushed antes: $").append(pushedAntes).append("\n");
            if (playerIndex == 0) {
                player1PushedAntes = 0; 
            }else {
                player2PushedAntes = 0;
            }
        }
    }

    private void revealDealerCards() {
        for (javafx.scene.Node node : dealerCards.getChildren()) {
            if (node instanceof StackPane) {
                setCardFaceUp((StackPane) node);
            }
        }
    }

    private void checkPlayDecisions() {
        if (player1PlayDecisionMade && player2PlayDecisionMade) {
            // Reveal dealer's cards and evaluate the round
            revealDealerCards();
            evaluateRound();
        }
    }

    private void handlePlayerAction(boolean isPlay, boolean isPlayer1) {
        // Get the relevant player and UI elements based on which player is acting
        Player player = isPlayer1 ? playerOne : playerTwo;
        TextField anteField = isPlayer1 ? player1AnteField : player2AnteField;
        TextField pairPlusField = isPlayer1 ? player1PairPlusField : player2PairPlusField;
        Button playButton = isPlayer1 ? player1PlayButton : player2PlayButton;
        Button foldButton = isPlayer1 ? player1FoldButton : player2FoldButton;
        HBox playerCards = isPlayer1 ? player1Cards : player2Cards;
        boolean initialBetMade = isPlayer1 ? player1InitialBetMade : player2InitialBetMade;

        if (!initialBetMade) {
            // Handle initial ante and pair plus bets
            try {
                int anteBet = Integer.parseInt(anteField.getText());
                int pairPlusBet = 0;

                // Validate ante bet
                if (anteBet < 5 || anteBet > 25) {
                    gameInfoLabel.setText("Ante bet must be between $5 and $25");
                    return;
                }

                // Validate pair plus bet if entered
                if (!pairPlusField.getText().isEmpty()) {
                    pairPlusBet = Integer.parseInt(pairPlusField.getText());
                    if (pairPlusBet < 5 || pairPlusBet > 25) {
                        gameInfoLabel.setText("Pair Plus bet must be between $5 and $25");
                        return;
                    }
                }

                // Update player bets
                player.anteBet = anteBet;
                player.pairPlusBet = pairPlusBet;
                if (isPlayer1) {
                    player1InitialBetMade = true;
                } else {
                    player2InitialBetMade = true;
                }

                // Update UI for play decision
                anteField.setDisable(true);
                pairPlusField.setDisable(true);
                playButton.setText("Make Play Bet (" + anteBet + ")");
                foldButton.setText("Fold");

                // Reveal player's cards
                for (javafx.scene.Node node : playerCards.getChildren()) {
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
            // Handle play/fold decision
            if (isPlay) {
                player.playBet = player.anteBet;
            } else {
                player.anteBet = 0;
                player.playBet = 0;
                player.pairPlusBet = 0;
            }

            if (isPlayer1) {
                player1PlayDecisionMade = true;
            } else {
                player2PlayDecisionMade = true;
            }

            playButton.setDisable(true);
            foldButton.setDisable(true);
            String buttonText = isPlay ? "Play Bet Made" : "Folded";
            playButton.setText(buttonText);
            foldButton.setText(buttonText);

            checkPlayDecisions();
        }
    }

    // Updated setupPlayerButtons method
    private void setupPlayerButtons(boolean isPlayer1) {
        if (isPlayer1) {
            player1PlayButton.setOnAction(e -> handlePlayerAction(true, true));
            player1FoldButton.setOnAction(e -> handlePlayerAction(false, true));
        } else {
            player2PlayButton.setOnAction(e -> handlePlayerAction(true, false));
            player2FoldButton.setOnAction(e -> handlePlayerAction(false, false));
        }
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.getStyleClass().add("menu-bar");
        menuBar.setUseSystemMenuBar(false);

        Menu optionsMenu = new Menu("Options");
        optionsMenu.getStyleClass().add("menu");

        MenuItem[] menuItems = createMenuItems();
        optionsMenu.getItems().addAll(menuItems);
        menuBar.getMenus().add(optionsMenu);

        return menuBar;
    }

    private void returnToGame() {
        primaryStage.setScene(gameScene);
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

    private void showExitScreen() {
        if (exitScene == null) {
            createExitScreen();
        }
        primaryStage.setScene(exitScene);
    }

    private void updateWinningsDisplays() {
        for (boolean isPlayer1 : new boolean[]{true, false}) {
            Label winningsLabel = isPlayer1 ? player1TotalWinningsLabel : player2TotalWinningsLabel;
            int totalWinnings = isPlayer1 ? playerOne.totalWinnings : playerTwo.totalWinnings;

            winningsLabel.getStyleClass().add("winnings-label");
            winningsLabel.setText(String.format("Total Winnings: $%d", Math.max(0, totalWinnings)));
        }
    }

    private void resetGame() {
        // Reset player states
        Player[] players = {playerOne, playerTwo};
        for (Player player : players) {
            player.totalWinnings = 0;
        }

        // Reset pushed antes
        player1PushedAntes = 0;
        player2PushedAntes = 0;

        // Reset game flags
        player1InitialBetMade = false;
        player2InitialBetMade = false;
        player1PlayDecisionMade = false;
        player2PlayDecisionMade = false;

        // Reset UI controls for both players
        for (boolean isPlayer1 : new boolean[]{true, false}) {
            // Get player-specific controls
            TextField anteField = isPlayer1 ? player1AnteField : player2AnteField;
            TextField pairPlusField = isPlayer1 ? player1PairPlusField : player2PairPlusField;
            Button playButton = isPlayer1 ? player1PlayButton : player2PlayButton;
            Button foldButton = isPlayer1 ? player1FoldButton : player2FoldButton;

            // Reset fields and buttons
            anteField.clear();
            pairPlusField.clear();
            playButton.setText("Play");
            foldButton.setText("Fold");

            // Enable controls
            setPlayerControlsEnabled(isPlayer1, true);
        }

        // Reset game state and UI
        gameInfoLabel.setText("Players: Place your bets");
        dealerArea.getChildren().removeIf(node
                -> node instanceof HBox && ((HBox) node).getStyleClass().contains("results-container")
        );
        playAgainBox.setVisible(false);

        // Update displays
        updateWinningsDisplays();
        updatePushedAntesDisplay();

        // Start new round
        startNewRound();
    }

    private MenuItem[] createMenuItems() {
        MenuItem exitItem = createMenuItem("Exit", e -> showExitScreen());
        MenuItem freshStartItem = createMenuItem("Fresh Start", e -> resetGame());
        MenuItem rulesItem = createMenuItem("Game Rules", e -> showRulesDialog());
        newLookItem = createMenuItem("Switch to Light Theme", e -> toggleTheme());

        return new MenuItem[]{exitItem, freshStartItem, rulesItem, newLookItem};
    }

    private MenuItem createMenuItem(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        MenuItem item = new MenuItem(text);
        item.getStyleClass().add("menu-item");
        item.setOnAction(handler);
        return item;
    }

    private Label createStyledLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }

    private void showGameScreen() {
        primaryStage.setScene(gameScene);
        primaryStage.setTitle("Three Card Poker - Game");
        if (gameScene.getStylesheets().isEmpty()) {
            String css = getClass().getResource("/css/dark-theme.css").toExternalForm();
            gameScene.getStylesheets().add(css);
        }
    }

    private void updateTotalWinningsLabelStyles() {
        player1TotalWinningsLabel.getStyleClass().add("winnings-label");
        player2TotalWinningsLabel.getStyleClass().add("winnings-label");
    }

    private void updatePushedAntesDisplay() {
        player1PushedAntesLabel.setText(String.format("Pushed Antes: $%d", player1PushedAntes));
        player2PushedAntesLabel.setText(String.format("Pushed Antes: $%d", player2PushedAntes));
    }

    private void setPlayerControlsEnabled(boolean isPlayer1, boolean enabled) {
        TextField anteField = isPlayer1 ? player1AnteField : player2AnteField;
        TextField pairPlusField = isPlayer1 ? player1PairPlusField : player2PairPlusField;
        Button playButton = isPlayer1 ? player1PlayButton : player2PlayButton;
        Button foldButton = isPlayer1 ? player1FoldButton : player2FoldButton;

        anteField.setDisable(!enabled);
        pairPlusField.setDisable(!enabled);
        playButton.setDisable(!enabled);
        foldButton.setDisable(!enabled);
    }

    // Optimized resetUI method
    private void resetUI() {
        // Get player controls for both players
        TextField[] anteFields = {player1AnteField, player2AnteField};
        TextField[] pairPlusFields = {player1PairPlusField, player2PairPlusField};
        Button[] playButtons = {player1PlayButton, player2PlayButton};
        Button[] foldButtons = {player1FoldButton, player2FoldButton};

        // Reset text fields and buttons for both players
        for (TextField field : anteFields) {
            field.clear();
        }
        for (TextField field : pairPlusFields) {
            field.clear();
        }
        for (Button button : playButtons) {
            button.setText("Play");
        }
        for (Button button : foldButtons) {
            button.setText("Fold");
        }

        // Enable controls for both players
        setPlayerControlsEnabled(true, true);
        setPlayerControlsEnabled(false, true);

        // Reset game info
        gameInfoLabel.setText("Players: Place your ante bets");
        playAgainBox.setVisible(false);
        dealButton.setDisable(true);
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

    private void showStartScreen() {
        primaryStage.setScene(startScene);
        primaryStage.setTitle("Three Card Poker");
    }
}
