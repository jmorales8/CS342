
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
        primaryStage.setOnCloseRequest(e -> {
            e.consume(); // Prevent default close
            showExitScreen();
        });
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
                portField);
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

    private void monitorMoneyChanges(PokerInfo info) {
        System.out.println("====== Money Update Received ======");
        System.out.println("Previous Total: $" + player.totalWinnings);
        System.out.println("New Total: $" + info.getTotalWinnings());
        System.out.println("Change: $" + (info.getTotalWinnings() - player.totalWinnings));
        System.out.println("Pushed Antes: $" + info.getPushedAntes());
        System.out.println("Result Message: " + info.getResultMessage());
        System.out.println("================================");
    }

    private void handleServerResponse(PokerInfo info) {
        monitorMoneyChanges(info);

        switch (info.getMessageType()) {
            case DEAL_CARDS:
                displayCards(playerCards, info.getPlayerCards(), false);
                displayCards(dealerCards, info.getDealerCards(), true);
                gameInfoLabel.setText("Cards dealt - make your play decision");
                break;

            case DEALER_CARDS:
                displayCards(dealerCards, info.getDealerCards(), false);
                gameInfoLabel.setText(info.getResultMessage());

                Platform.runLater(() -> {
                    System.out.println("Updating UI with new totals:");
                    System.out.println("Setting total winnings to: $" + info.getTotalWinnings());

                    player.totalWinnings = info.getTotalWinnings();
                    playerPushedAntes = info.getPushedAntes();

                    updateWinningsDisplay();
                    updatePushedAntesDisplay();

                    System.out.println("After update - Label text: " + playerTotalWinningsLabel.getText());
                    System.out.println("After update - Player total: $" + player.totalWinnings);

                    playAgainBox.setVisible(true);
                    playAgainBox.setManaged(true);
                });
                break;
        }
    }

    private void startNewRound() {
        try {
            // Send new game request to server
            out.writeObject(PokerInfo.newGame());
            out.flush();

            // Reset UI
            initialBetMade = false;
            playDecisionMade = false;
            dealerCards.getChildren().clear();
            playerCards.getChildren().clear();
            resetUI();

        } catch (IOException e) {
            gameInfoLabel.setText("Error starting new game");
        }
    }

    private void displayCards(HBox container, ArrayList<Card> cards, boolean faceDown) {
        container.getChildren().clear();
        for (Card card : cards) {
            StackPane cardNode = createCardNode(card, faceDown);
            container.getChildren().add(cardNode);
        }
    }

    private void listenForServerMessages() {
        try {
            while (socket != null && !socket.isClosed()) {
                Object message = in.readObject();
                if (message instanceof PokerInfo) {
                    PokerInfo info = (PokerInfo) message;
                    Platform.runLater(() -> handleServerResponse(info));
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error receiving server message: " + e.getMessage());
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
                        "• Dealer must have Queen-high or better to qualify\n" + "• If dealer doesn't qualify:\n"
                                + "  - Play bet is returned\n" + "  - Ante bet is pushed to next hand\n"
                                + "• If dealer qualifies, highest poker hand wins"),
                createRuleSection("Hand Rankings (Highest to Lowest):",
                        "1. Straight Flush\n" + "2. Three of a Kind\n" + "3. Straight\n" + "4. Flush\n" + "5. Pair\n"
                                + "6. High Card"),
                createRuleSection("Pair Plus Payouts:",
                        "• Straight Flush: 40 to 1\n" + "• Three of a Kind: 30 to 1\n" + "• Straight: 6 to 1\n"
                                + "• Flush: 3 to 1\n" + "• Pair: 1 to 1"),
                createRuleSection("Important Notes:",
                        "• Pair Plus bet wins regardless of dealer's hand\n"
                                + "• Ante and Play bets pay 1 to 1 on wins\n"
                                + "• Folding forfeits both Ante and Pair Plus bets\n"
                                + "• Ace can be high or low in straights"));

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
        BorderPane startRoot = new BorderPane();
        startRoot.getStyleClass().add("root");

        VBox contentBox = new VBox(20);
        contentBox.setAlignment(Pos.CENTER);

        Text titleText = createStyledText("Welcome to Three Card Poker", "title-text");
        Button[] buttons = createStartButtons();

        contentBox.getChildren().add(titleText);
        contentBox.getChildren().addAll(buttons);

        startRoot.setCenter(contentBox);
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

        return new Button[] { startButton, rulesButton, exitButton };
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

        return cardVisual;
    }

    private Button createStyledButton(String text, String styleClass,
            javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
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
        betContainer.setAlignment(Pos.CENTER);

        HBox anteBox = new HBox(5);
        HBox pairPlusBox = new HBox(5);
        anteBox.setAlignment(Pos.CENTER);
        pairPlusBox.setAlignment(Pos.CENTER);

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
        VBox area = new VBox(20);
        area.setAlignment(Pos.CENTER);
        area.setPrefWidth(SCENE_WIDTH);

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
        button.getStyleClass().clear();
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
        playerPlayButton = createStyledButton("Play", "play-button");
        playerFoldButton = createStyledButton("Fold", "fold-button");
        playerTotalWinningsLabel = createStyledLabel("Total Winnings: $0", "winnings-label");
        playerPushedAntesLabel = createStyledLabel("Pushed Antes: $0", "pushed-antes-label");

        HBox buttonContainer = createPlayerButtonContainer();
        setupPlayerButtons();

        // Add all components to player area
        area.getChildren().addAll(
                playerLabel,
                playerTotalWinningsLabel,
                playerPushedAntesLabel,
                playerCards,
                createBetFields(),
                buttonContainer);

        return area;
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

        dealButton = createDealButton();
        dealButton.setVisible(false);
        dealButton.setManaged(false);

        menuBar.setPrefWidth(SCENE_WIDTH);
        menuBar.setMinHeight(30);

        // Add menuBar and deal button
        topContainer.getChildren().addAll(menuBar, dealButton);
        return topContainer;
    }

    private Button createDealButton() {
        Button button = new Button("Deal New Hand");
        button.getStyleClass().add("primary-button");
        button.setMaxWidth(200);
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

    private void handlePlayerAction(boolean isPlay) {
        if (!initialBetMade) {
            try {
                int anteBet = Integer.parseInt(playerAnteField.getText());
                player.anteBet = anteBet; // Store ante bet
                int pairPlusBet = 0;

                if (!playerPairPlusField.getText().isEmpty()) {
                    pairPlusBet = Integer.parseInt(playerPairPlusField.getText());
                    player.pairPlusBet = pairPlusBet; // Store pair plus bet
                }

                PokerInfo betInfo = new PokerInfo();
                betInfo.setMessageType(PokerInfo.MessageType.PLACE_BETS);
                betInfo.setAnteBet(anteBet);
                betInfo.setPairPlusBet(pairPlusBet);

                out.writeObject(betInfo);
                out.flush();

                playerAnteField.setDisable(true);
                playerPairPlusField.setDisable(true);
                initialBetMade = true;

            } catch (IOException e) {
                System.err.println("Error sending bets to server: " + e.getMessage());
            }
        } else {
            try {
                PokerInfo decision = new PokerInfo();
                decision.setMessageType(PokerInfo.MessageType.PLAY_DECISION);
                decision.setPlayerFolded(!isPlay);
                decision.setAnteBet(player.anteBet);
                decision.setPairPlusBet(player.pairPlusBet);
                decision.setPlayBet(isPlay ? player.anteBet : 0);

                out.writeObject(decision);
                out.flush();

                playDecisionMade = true;
                playerPlayButton.setDisable(true);
                playerFoldButton.setDisable(true);
                String buttonText = isPlay ? "Play Bet Made" : "Folded";
                playerPlayButton.setText(buttonText);
                playerFoldButton.setText(buttonText);

            } catch (IOException e) {
                gameInfoLabel.setText("Error communicating with server");
            }
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
        BorderPane exitRoot = new BorderPane();
        exitRoot.getStyleClass().add("exit-root");

        VBox contentBox = new VBox(20);
        contentBox.setAlignment(Pos.CENTER);

        Text exitText = new Text("Are you sure you want to exit?");
        exitText.getStyleClass().add("exit-text");

        Text standingsText = new Text(String.format(
                "Current Winnings: $%d",
                player.totalWinnings));
        standingsText.getStyleClass().add("standings-text");

        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button continueButton = new Button("Continue Playing");
        continueButton.getStyleClass().add("continue-button");
        continueButton.setOnAction(e -> returnToGame());

        Button quitButton = new Button("Quit Game");
        quitButton.getStyleClass().add("quit-button");
        quitButton.setOnAction(e -> closeConnectionAndExit());

        buttonBox.getChildren().addAll(continueButton, quitButton);
        contentBox.getChildren().addAll(exitText, standingsText, buttonBox);

        exitRoot.setCenter(contentBox);
        BorderPane.setMargin(contentBox, Insets.EMPTY);

        exitScene = new Scene(exitRoot, 500, 300);
        exitScene.getStylesheets().addAll(gameScene.getStylesheets());
    }

    private void showExitScreen() {
        if (exitScene == null) {
            createExitScreen();
        }
        primaryStage.setScene(exitScene);

        // Add window close handler after scene is set
        primaryStage.setOnCloseRequest(e -> {
            e.consume();
            closeConnectionAndExit();
        });
    }

    // Helper method to handle connection closing and exit
    private void closeConnectionAndExit() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ex) {
            System.err.println("Error closing connection: " + ex.getMessage());
        } finally {
            Platform.exit();
        }
    }

    private void updateWinningsDisplay() {
        playerTotalWinningsLabel.getStyleClass().add("winnings-label");

        playerTotalWinningsLabel.setText(String.format("Total Winnings: $%d", player.totalWinnings));
    }

    private void resetGame() {
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
        dealerArea.getChildren()
                .removeIf(node -> node instanceof HBox && ((HBox) node).getStyleClass().contains("results-container"));
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

        return new MenuItem[] { exitItem, freshStartItem, rulesItem, newLookItem };
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

    private void updatePushedAntesDisplay() {
        playerPushedAntesLabel.setText(String.format("Pushed Antes: $%d", playerPushedAntes));
    }

    private void setPlayerControlsEnabled(boolean enabled) {
        playerAnteField.setDisable(!enabled);
        playerPairPlusField.setDisable(!enabled);
        playerPlayButton.setDisable(!enabled);
        playerFoldButton.setDisable(!enabled);
    }

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

    public static void main(String[] args) {
        launch(args);
    }

}
