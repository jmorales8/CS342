
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
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
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class JavaFXTemplate extends Application {

    private static final double SCENE_WIDTH = 1200, SCENE_HEIGHT = 800,
            CARD_WIDTH = 100, CARD_HEIGHT = 140;

    private enum GameState {
        PLAYER_TURN, ROUND_COMPLETE
    }

    // Game components and state
    private final Player player = new Player();
    private final Dealer theDealer = new Dealer();
    private GameState currentState;
    private String currentTheme = "default";

    // Hands
    private ArrayList<Card> dealerHand, playerHand;

    // UI Stage and Scenes
    private Stage primaryStage;
    private Scene startScene, gameScene, exitScene;

    // UI Components - Dealer
    private Button dealButton;
    private HBox dealerCards;
    private VBox dealerArea;

    // UI Components - Player
    private TextField playerAnteField, playerPairPlusField;
    private Button playerPlayButton, playerFoldButton;
    private VBox playerArea;
    private HBox playerCards;
    private Label playerTotalWinningsLabel, playerPushedAntesLabel;
    private int playerPushedAntes = 0;

    // Additional UI and game state
    private HBox playAgainBox;
    private Label gameInfoLabel;
    private MenuItem newLookItem;
    private boolean initialBetMade, playDecisionMade;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Three Card Poker");

        initializeTheme();
        createScenes();
        showConnectionDialog();
        primaryStage.setScene(startScene);
        primaryStage.show();
    }

    private void toggleTheme() {
        gameScene.getStylesheets().clear();
        startScene.getStylesheets().clear();

        if ("default".equals(currentTheme)) {
            currentTheme = "light";
            loadTheme("/css/light-theme.css");
            newLookItem.setText("Switch to Dark Theme");
        } else {
            currentTheme = "default";
            loadTheme("/css/dark-theme.css");
            newLookItem.setText("Switch to Light Theme");
        }
    }

    private void loadTheme(String themePath) {
        try {
            String css = getClass().getResource(themePath).toExternalForm();
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

    private void showConnectionDialog() {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Server Connection");
        dialog.setHeaderText("Enter Server Details");
    
        // Create the dialog content
        TextField portField = new TextField("8080");
        portField.setPromptText("Port Number");
        
        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label("Port:"), 
            portField
        );
        dialog.getDialogPane().setContent(content);
    
        // Add buttons
        ButtonType connectButtonType = new ButtonType("Connect", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(connectButtonType, ButtonType.CANCEL);
    
        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == connectButtonType) {
                try {
                    return Integer.parseInt(portField.getText());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });
    
        // Show dialog and handle result
        Optional<Integer> result = dialog.showAndWait();
        result.ifPresent(port -> {
            connectToServer("localhost", port);
        });
    }

    private void connectToServer(String host, int port) {
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            new Thread(this::listenForServerMessages).start();
            System.out.println("Connected to server");
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
        }
    }

    private void listenForServerMessages() {
        try {
            while (socket != null && !socket.isClosed()) {
                Object message = in.readObject();
                if (message != null) {
                    Platform.runLater(() -> {
                        System.out.println("Received from server: " + message);
                    });
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error receiving server message: " + e.getMessage());
        }
    }

    private void pingServer() {
        if (out != null) {
            try {
                out.writeObject("PING");
                out.flush();
                System.out.println("Ping sent to server");
            } catch (IOException e) {
                System.err.println("Error pinging server: " + e.getMessage());
            }
        } else {
            System.err.println("Not connected to server");
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
        if (cardPane instanceof CardVisual) {
            ((CardVisual) cardPane).setFaceUp();
        }
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
        BorderPane startRoot = new BorderPane();  // Change to BorderPane
        startRoot.getStyleClass().add("root");

        VBox contentBox = new VBox(20);
        contentBox.setAlignment(Pos.CENTER);

        Text titleText = createStyledText("Welcome to Three Card Poker", "title-text");
        Button[] buttons = createStartButtons();

        contentBox.getChildren().add(titleText);
        contentBox.getChildren().addAll(buttons);

        startRoot.setCenter(contentBox);  // Center the content
        // Remove any margins
        BorderPane.setMargin(contentBox, Insets.EMPTY);

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

    private boolean isCardFaceUp(StackPane cardPane) {
        try {
            Object[] data = (Object[]) cardPane.getUserData();
            if (data != null) {
                VBox content = (VBox) data[0];
                return content.isVisible();
            }
        } catch (Exception e) {
            System.err.println("Error checking card face: " + e.getMessage());
        }
        return false;
    }

    private StackPane createCardNode(Card card, boolean isFaceDown) {
        // Create visual representation using CardVisual class
        CardVisual cardVisual = new CardVisual(card);

        // Set initial state
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

    private VBox createBetFields() {
        VBox betContainer = new VBox(5);
        betContainer.setAlignment(Pos.CENTER); // Center alignment

        HBox anteBox = new HBox(5);
        HBox pairPlusBox = new HBox(5);
        anteBox.setAlignment(Pos.CENTER); // Center the ante box
        pairPlusBox.setAlignment(Pos.CENTER); // Center the pair plus box

        Label anteLabel = createStyledLabel("Ante:", "player-label");
        Label pairPlusLabel = createStyledLabel("Pair+:", "player-label");

        // Set preferred widths for consistent layout
        playerAnteField.setPrefWidth(150);
        playerPairPlusField.setPrefWidth(150);

        anteBox.getChildren().addAll(anteLabel, playerAnteField);
        pairPlusBox.getChildren().addAll(pairPlusLabel, playerPairPlusField);

        betContainer.getChildren().addAll(anteBox, pairPlusBox);
        return betContainer;
    }

    private VBox createMainContent() {
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.getStyleClass().add("root");

        dealerArea = createDealerArea();
        playerArea = createPlayerArea();

        mainContent.getChildren().addAll(dealerArea, playerArea);
        return mainContent;
    }

    private VBox createDealerArea() {
        VBox area = new VBox(20);  // Increased spacing between elements
        area.setAlignment(Pos.CENTER);
        area.setPrefWidth(SCENE_WIDTH);  // Ensure full width

        // Create play again box first
        createPlayAgainBox();

        // Create other dealer area components
        Label dealerLabel = createStyledLabel("Dealer's Cards", "dealer-label");
        dealerCards = createCardContainer();
        gameInfoLabel = createStyledLabel("Players: Place your bets", "game-info-label");

        // Add components in correct order
        area.getChildren().addAll(playAgainBox, dealerLabel, dealerCards, gameInfoLabel);

        return area;
    }

    private void setupPlayerButtons() {
        playerPlayButton.setOnAction(e -> handlePlayerAction(true));
        playerFoldButton.setOnAction(e -> handlePlayerAction(false));
    }

    private HBox createPlayerButtonContainer() {
        HBox container = new HBox(10);
        container.getStyleClass().add("button-container");
        container.getChildren().addAll(playerPlayButton, playerFoldButton);
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

    private VBox createPlayerArea() {
        VBox area = new VBox(10);
        area.getStyleClass().add("player-area");

        // Create components
        Label playerLabel = createStyledLabel("Player Cards", "player-label");
        playerCards = createCardContainer();

        // Initialize player fields
        playerAnteField = createStyledTextField("Enter Ante Bet");
        playerPairPlusField = createStyledTextField("Enter Pair Plus Bet");
        playerPlayButton = createStyledButton("Play", "#4CAF50");
        playerFoldButton = createStyledButton("Fold", "#f44336");
        playerTotalWinningsLabel = createStyledLabel("Total Winnings: $0", "winnings-label");
        playerPushedAntesLabel = createStyledLabel("Pushed Antes: $0", "pushed-antes-label");

        // Create ping server button
        Button pingServerButton = createStyledButton("Ping Server", "#2196F3");
        pingServerButton.setOnAction(e -> pingServer());

        HBox buttonContainer = createPlayerButtonContainer();
        setupPlayerButtons();

        // Add ping button to button container
        buttonContainer.getChildren().add(pingServerButton);

        // Add all components to player area
        area.getChildren().addAll(
                playerLabel,
                playerTotalWinningsLabel,
                playerPushedAntesLabel,
                playerCards,
                createBetFields(),
                buttonContainer
        );

        return area;
    }

// Helper method to show messages (add this if you don't have it)
    private void showMessage(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Server Communication");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.show();
        });
    }

    private HBox createCardContainer() {
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER);
        container.getStyleClass().add("card-container");
        return container;
    }

    private VBox createTopContainer(MenuBar menuBar) {
        VBox topContainer = new VBox(0);
        topContainer.setAlignment(Pos.TOP_CENTER);

        // Create the deal button instead of back button
        dealButton = createDealButton();
        dealButton.setVisible(false); // Initially invisible
        dealButton.setManaged(false); // Initially not taking space

        menuBar.setPrefWidth(SCENE_WIDTH);
        menuBar.setMinHeight(30);

        // Add menuBar and deal button
        topContainer.getChildren().addAll(menuBar, dealButton);
        return topContainer;
    }
    // Update createDealButton() method

    private Button createDealButton() {
        Button button = new Button("Deal New Hand");
        button.getStyleClass().add("primary-button"); // Changed from danger-button for better UX
        button.setMaxWidth(200); // Set a specific max width for better appearance
        button.setOnAction(e -> {
            // Reset game state
            initialBetMade = false;
            playDecisionMade = false;

            // Clear previous hands
            dealerHand = null;
            playerHand = null;

            // Reset UI elements
            dealerCards.getChildren().clear();
            playerCards.getChildren().clear();

            // Enable betting fields
            playerAnteField.setDisable(false);
            playerPairPlusField.setDisable(false);

            // Reset buttons
            playerPlayButton.setDisable(false);
            playerFoldButton.setDisable(false);
            playerPlayButton.setText("Play");
            playerFoldButton.setText("Fold");

            // Clear bet fields
            playerAnteField.clear();
            playerPairPlusField.clear();

            // Hide play again box
            playAgainBox.setVisible(false);
            playAgainBox.setManaged(false);

            // Start new round
            startNewRound();

            // Update game info
            gameInfoLabel.setText("Player: Place your bets");

            // Update displays
            updateWinningsDisplay();
            updatePushedAntesDisplay();
        });
        return button;
    }

// Update createPlayAgainBox() for better positioning
    private void createPlayAgainBox() {
        playAgainBox = new HBox(10);
        playAgainBox.setAlignment(Pos.CENTER);
        playAgainBox.setPadding(new Insets(10));
        playAgainBox.setVisible(false);
        playAgainBox.setManaged(false);

        Label playAgainLabel = createStyledLabel("Play Another Hand?", "player-label");
        dealButton = createDealButton();

        playAgainBox.getChildren().addAll(playAgainLabel, dealButton);
    }

    private void checkInitialBet() {
        if (initialBetMade) {
            gameInfoLabel.setText("Player: Review your cards and make play bet or fold");
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

        // Handle Pair Plus bet first
        int pairPlusWinnings = ThreeCardLogic.evalPPWinnings(playerHand, player.pairPlusBet);
        player.totalWinnings += pairPlusWinnings;

        // Get the game result
        String gameResult = determineWinner(dealerHand, playerHand, player.anteBet, player.playBet);

        // Create results display
        VBox resultsBox = new VBox(5);
        resultsBox.setAlignment(Pos.CENTER);  // Center the VBox contents
        StringBuilder result = new StringBuilder();

        result.append("Results:\n");
        if (pairPlusWinnings > 0) {
            result.append("Pair Plus Winnings: $").append(pairPlusWinnings).append("\n");
        }
        result.append(gameResult).append("\n");

        // Process results based on game outcome
        if (gameResult.contains("Dealer does not qualify")) {
            handleDealerNotQualified(result, player);
        } else if (gameResult.contains("You win")) {
            handlePlayerWin(result, player);
        } else if (gameResult.contains("Dealer wins")) {
            handlePlayerLoss(result, player);
        }

        // Display results with centered text
        Label resultLabel = new Label(result.toString());
        resultLabel.setAlignment(Pos.CENTER);  // Center the text within the label
        resultLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);  // Center-align the text content
        resultLabel.setMaxWidth(Double.MAX_VALUE);  // Allow label to take full width
        resultLabel.getStyleClass().add("game-info-text");

        resultsBox.getChildren().add(resultLabel);

        // Update UI
        gameInfoLabel.setText("Round Complete");
        dealerArea.getChildren().removeIf(node
                -> node instanceof HBox && ((HBox) node).getStyleClass().contains("results-container")
        );
        dealerArea.getChildren().add(resultsBox);

        updatePushedAntesDisplay();
        updateWinningsDisplay();
        playAgainBox.setVisible(true);
        playAgainBox.setManaged(true);
    }

    private void handleDealerNotQualified(StringBuilder result, Player player) {
        result.append("Play bet returned: $").append(player.playBet).append("\n");
        playerPushedAntes += player.anteBet;
        result.append("Ante pushes to next hand (Total pushed: $")
                .append(playerPushedAntes).append(")\n");
        player.playBet = 0;
    }

    private void handlePlayerWin(StringBuilder result, Player player) {
        int mainGameWinnings = (player.anteBet + player.playBet);
        player.totalWinnings += mainGameWinnings;

        if (playerPushedAntes > 0) {
            player.totalWinnings += playerPushedAntes;
            result.append("Won pushed antes: $").append(playerPushedAntes).append("\n");
            playerPushedAntes = 0;
        }
        result.append("Main Game Winnings: $").append(mainGameWinnings).append("\n");
    }

    private void handlePlayerLoss(StringBuilder result, Player player) {
        result.append("Main Game Loss\n");
        if (playerPushedAntes > 0) {
            result.append("Lost pushed antes: $").append(playerPushedAntes).append("\n");
            playerPushedAntes = 0;
        }
    }

    private void checkPlayDecision() {
        if (playDecisionMade) {
            revealDealerCards();
            evaluateRound();
        }
    }

    private void revealDealerCards() {
        for (javafx.scene.Node node : dealerCards.getChildren()) {
            if (node instanceof CardVisual) {
                ((CardVisual) node).setFaceUp();
            }
        }
    }

    private void handlePlayerAction(boolean isPlay) {
        if (!initialBetMade) {
            try {
                int anteBet = Integer.parseInt(playerAnteField.getText());
                int pairPlusBet = 0;

                if (anteBet < 5 || anteBet > 25) {
                    gameInfoLabel.setText("Ante bet must be between $5 and $25");
                    return;
                }

                if (!playerPairPlusField.getText().isEmpty()) {
                    pairPlusBet = Integer.parseInt(playerPairPlusField.getText());
                    if (pairPlusBet < 5 || pairPlusBet > 25) {
                        gameInfoLabel.setText("Pair Plus bet must be between $5 and $25");
                        return;
                    }
                }

                player.anteBet = anteBet;
                player.pairPlusBet = pairPlusBet;
                initialBetMade = true;

                playerAnteField.setDisable(true);
                playerPairPlusField.setDisable(true);
                playerPlayButton.setText("Make Play Bet (" + anteBet + ")");
                playerFoldButton.setText("Fold");

                for (javafx.scene.Node node : playerCards.getChildren()) {
                    if (node instanceof StackPane) {
                        setCardFaceUp((StackPane) node);
                    }
                }

                checkInitialBet(); // Call the helper method here
            } catch (NumberFormatException e) {
                gameInfoLabel.setText("Please enter valid bets");
            }
        } else {
            if (isPlay) {
                player.playBet = player.anteBet;
            } else {
                player.anteBet = 0;
                player.playBet = 0;
                player.pairPlusBet = 0;
            }

            playDecisionMade = true;
            playerPlayButton.setDisable(true);
            playerFoldButton.setDisable(true);
            String buttonText = isPlay ? "Play Bet Made" : "Folded";
            playerPlayButton.setText(buttonText);
            playerFoldButton.setText(buttonText);

            checkPlayDecision(); // Call the helper method here
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

        // Ensure menu bar fills width
        menuBar.setPrefWidth(SCENE_WIDTH);

        return menuBar;
    }

    private void returnToGame() {
        primaryStage.setScene(gameScene);
    }

    private void createExitScreen() {
        BorderPane exitRoot = new BorderPane();  // Change to BorderPane
        exitRoot.getStyleClass().add("exit-root");

        VBox contentBox = new VBox(20);
        contentBox.setAlignment(Pos.CENTER);

        // Title
        Text exitText = new Text("Are you sure you want to exit?");
        exitText.getStyleClass().add("exit-text");

        // Subtitle with current winnings
        Text standingsText = new Text(String.format(
                "Current Winnings: $%d",
                player.totalWinnings
        ));
        standingsText.getStyleClass().add("standings-text");

        // Buttons container
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button continueButton = new Button("Continue Playing");
        continueButton.getStyleClass().add("continue-button");
        continueButton.setOnAction(e -> returnToGame());

        Button quitButton = new Button("Quit Game");
        quitButton.getStyleClass().add("quit-button");
        quitButton.setOnAction(e -> Platform.exit());

        buttonBox.getChildren().addAll(continueButton, quitButton);
        contentBox.getChildren().addAll(exitText, standingsText, buttonBox);

        exitRoot.setCenter(contentBox);  // Center the content
        // Remove any margins
        BorderPane.setMargin(contentBox, Insets.EMPTY);

        exitScene = new Scene(exitRoot, 500, 300);
        exitScene.getStylesheets().addAll(gameScene.getStylesheets());
    }

    private void showExitScreen() {
        if (exitScene == null) {
            createExitScreen();
        }
        primaryStage.setScene(exitScene);
    }

    private void updateWinningsDisplay() {
        playerTotalWinningsLabel.getStyleClass().add("winnings-label");
        playerTotalWinningsLabel.setText(String.format("Total Winnings: $%d", Math.max(0, player.totalWinnings)));
    }

    private void resetGame() {
        // Reset player state
        player.totalWinnings = 0;
        playerPushedAntes = 0;

        // Reset game flags
        initialBetMade = false;
        playDecisionMade = false;

        // Reset UI controls
        playerAnteField.clear();
        playerPairPlusField.clear();
        playerPlayButton.setText("Play");
        playerFoldButton.setText("Fold");

        // Enable controls
        setPlayerControlsEnabled(true);

        // Reset game state and UI
        gameInfoLabel.setText("Player: Place your bet");
        dealerArea.getChildren().removeIf(node
                -> node instanceof HBox && ((HBox) node).getStyleClass().contains("results-container")
        );
        playAgainBox.setVisible(false);

        // Update displays
        updateWinningsDisplay();
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

    private void updateTotalWinningsLabelStyle() {
        playerTotalWinningsLabel.getStyleClass().add("winnings-label");
    }

    private void updatePushedAntesDisplay() {
        playerPushedAntesLabel.setText(String.format("Pushed Antes: $%d", playerPushedAntes));
    }

    private void setPlayerControlsEnabled(boolean enabled) {
        playerAnteField.setDisable(!enabled);
        playerPairPlusField.setDisable(!enabled);
        playerPlayButton.setDisable(!enabled);
        playerFoldButton.setDisable(!enabled);
    }
    // Optimized resetUI method

    private void resetUI() {
        // Reset text fields and buttons
        playerAnteField.clear();
        playerPairPlusField.clear();
        playerPlayButton.setText("Play");
        playerFoldButton.setText("Fold");

        // Enable controls
        setPlayerControlsEnabled(true);

        // Reset game info
        gameInfoLabel.setText("Player: Place your ante bet");
        playAgainBox.setVisible(false);
        dealButton.setDisable(true);
    }

    private void startNewRound() {
        // Reset betting flags
        initialBetMade = false;
        playDecisionMade = false;

        // Deal cards
        dealerHand = theDealer.dealHand();
        playerHand = theDealer.dealHand();

        // Clear and reset displays
        dealerCards.getChildren().clear();
        playerCards.getChildren().clear();

        // Clear previous results
        dealerArea.getChildren().removeIf(node
                -> node instanceof VBox
                || (node instanceof HBox && ((HBox) node).getStyleClass().contains("results-container"))
        );

        // Add cards face down initially
        for (int i = 0; i < 3; i++) {
            StackPane dealerCard = createCardNode(dealerHand.get(i), true);
            StackPane playerCard = createCardNode(playerHand.get(i), true);

            dealerCards.getChildren().add(dealerCard);
            playerCards.getChildren().add(playerCard);
        }

        // Reset UI
        updateTotalWinningsLabelStyle();
        updatePushedAntesDisplay();
        resetUI();
    }

    private void showStartScreen() {
        primaryStage.setScene(startScene);
        primaryStage.setTitle("Three Card Poker");
    }

    public static void main(String[] args) {
        launch(args);
    }

}
