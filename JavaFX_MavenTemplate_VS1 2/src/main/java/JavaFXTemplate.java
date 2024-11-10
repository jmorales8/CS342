
import java.util.ArrayList;
import java.util.Arrays;

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

    // Game state
    private enum GameState {
        PLAYER_ONE_TURN,
        PLAYER_TWO_TURN,
        ROUND_COMPLETE
    }
    private GameState currentState;

    // Game state tracking
    private boolean player1Submitted = false;
    private boolean player2Submitted = false;
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

        startButton.setOnAction(e -> {
            showGameScreen();
            startNewRound(); // Start a new round immediately when game screen is shown
        });
        exitButton.setOnAction(e -> Platform.exit());

        startRoot.getChildren().addAll(titleText, startButton, exitButton);
        startScene = new Scene(startRoot, 1200, 800);
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
        scrollPane.setFitToWidth(true);

        // Main content container
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setStyle("-fx-background-color: #1a1a1a;");

        // Dealer area at top with title
        VBox dealerArea = new VBox(10);
        dealerArea.setAlignment(Pos.CENTER);
        Label dealerLabel = new Label("Dealer's Cards");
        dealerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        dealerCards = new HBox(10);
        dealerCards.setAlignment(Pos.CENTER);
        dealerArea.getChildren().addAll(dealerLabel, dealerCards);

        // Game info display
        gameInfoLabel = new Label("Players: Place your bets");
        gameInfoLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-size: 18px;");
        dealerArea.getChildren().add(gameInfoLabel);

        // Create horizontal container for both players
        HBox playersContainer = new HBox(50);
        playersContainer.setAlignment(Pos.CENTER);

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
// Now add the total winnings labels with their green style
        player1TotalWinningsLabel = new Label("Total Winnings: $0");
        player1TotalWinningsLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-size: 14px; -fx-font-weight: bold;");

        HBox player1Buttons = new HBox(10, player1PlayButton, player1FoldButton);
        player1Area.getChildren().addAll(
                player1Label,
                player1TotalWinningsLabel,
                player1Cards,
                new Label("Ante:"), player1AnteField,
                new Label("Pair+:"), player1PairPlusField,
                player1Buttons
        );

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
        player2TotalWinningsLabel = new Label("Total Winnings: $0");
        player2TotalWinningsLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-size: 14px; -fx-font-weight: bold;");

        HBox player2Buttons = new HBox(10, player2PlayButton, player2FoldButton);
        player2Area.getChildren().addAll(
                player2Label,
                player2TotalWinningsLabel,
                player2Cards,
                new Label("Ante:"), player2AnteField,
                new Label("Pair+:"), player2PairPlusField,
                player2Buttons
        );

        // Style all labels in player areas
// Style all labels in player areas
        for (VBox playerArea : Arrays.asList(player1Area, player2Area)) {
            playerArea.getChildren().forEach(node -> {
                if (node instanceof Label) {
                    ((Label) node).setStyle("-fx-text-fill: white;");
                }
            });
        }

        // Add players to horizontal container
        playersContainer.getChildren().addAll(player1Area, player2Area);

        // Create deal button and play again label
        dealButton = new Button("Deal");
        dealButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
        Label playAgainLabel = new Label("Play Again?");
        playAgainLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

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
        backButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
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

        gameScene = new Scene(gameRoot, 1200, 800);
        dealButton.setOnAction(e -> {
            playAgainBox.setVisible(false);

            // Reset player bets and states
            playerOne.anteBet = 0;
            playerOne.playBet = 0;
            playerOne.pairPlusBet = 0;
            playerTwo.anteBet = 0;
            playerTwo.playBet = 0;
            playerTwo.pairPlusBet = 0;

            // Reset betting flags
            player1InitialBetMade = false;
            player2InitialBetMade = false;
            player1PlayDecisionMade = false;
            player2PlayDecisionMade = false;

            // Enable all controls
            setPlayer1ControlsEnabled(true);
            setPlayer2ControlsEnabled(true);

            // Reset text fields
            player1AnteField.clear();
            player1PairPlusField.clear();
            player2AnteField.clear();
            player2PairPlusField.clear();

            // Reset button text
            player1PlayButton.setText("Play");
            player2PlayButton.setText("Play");
            player1FoldButton.setText("Fold");
            player2FoldButton.setText("Fold");

            // Start new round
            startNewRound();
        });
        MenuBar menuBar = new MenuBar();
        Menu optionsMenu = new Menu("Options");

        MenuItem exitItem = new MenuItem("Exit");
        MenuItem freshStartItem = new MenuItem("Fresh Start");
        MenuItem newLookItem = new MenuItem("New Look");
        newLookItem.setOnAction(e -> toggleTheme());

        optionsMenu.getItems().addAll(exitItem, freshStartItem, newLookItem);
        menuBar.getMenus().add(optionsMenu);

        // Add event handlers
        exitItem.setOnAction(e -> showExitScreen());
        freshStartItem.setOnAction(e -> resetGame());
        newLookItem.setOnAction(e -> toggleTheme());

        // Add MenuBar to top of BorderPane
        VBox topContainer = new VBox();
        topContainer.getChildren().addAll(menuBar, playAgainBox);
        gameRoot.setTop(topContainer);
        updateTotalWinningsLabelStyles();
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

    private void resetGame() {
        playerOne.totalWinnings = 0;
        playerTwo.totalWinnings = 0;
        updateWinningsDisplays();
        startNewRound();
    }

    private void toggleTheme() {
        gameScene.getStylesheets().clear();
        if ("default".equals(currentTheme)) {
            currentTheme = "dark";
            gameScene.getStylesheets().add("dark.css");
        } else {
            currentTheme = "default";
            gameScene.getStylesheets().add("default.css");
        }
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
                // Make play bet equal to ante
                playerOne.playBet = playerOne.anteBet;
            } else {
                // Player folded
                playerOne.anteBet = 0;
                playerOne.playBet = 0;
                playerOne.pairPlusBet = 0;
            }

            player1PlayDecisionMade = true;
            player1PlayButton.setDisable(true);
            player1FoldButton.setDisable(true);

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
                // Make play bet equal to ante
                playerTwo.playBet = playerTwo.anteBet;
            } else {
                // Player folded
                playerTwo.anteBet = 0;
                playerTwo.playBet = 0;
                playerTwo.pairPlusBet = 0;
            }

            player2PlayDecisionMade = true;
            player2PlayButton.setDisable(true);
            player2FoldButton.setDisable(true);

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

        // Add all cards face down initially
        for (int i = 0; i < 3; i++) {
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
        int player1MainWinnings = calculateMainGameWinnings(player1Result, playerOne.anteBet);

        // Calculate Player 2 results
        String player2Result = ThreeCardLogic.determineWinner(dealerHand, player2Hand,
                playerTwo.anteBet, playerTwo.playBet);
        int player2PairPlus = ThreeCardLogic.evalPPWinnings(player2Hand, playerTwo.pairPlusBet);
        int player2MainWinnings = calculateMainGameWinnings(player2Result, playerTwo.anteBet);

        // Update totals - only add positive winnings to total
        if (player1MainWinnings > 0) {
            playerOne.totalWinnings += player1MainWinnings;
        }
        if (player1PairPlus > 0) {
            playerOne.totalWinnings += player1PairPlus;
        }

        if (player2MainWinnings > 0) {
            playerTwo.totalWinnings += player2MainWinnings;
        }
        if (player2PairPlus > 0) {
            playerTwo.totalWinnings += player2PairPlus;
        }

        // Display results
        finalResult.append("Player 1:\n");
        finalResult.append(player1Result).append("\n");
        if (player1MainWinnings > 0) {
            finalResult.append("Main Game Winnings: $").append(player1MainWinnings).append("\n");
        } else if (player1MainWinnings < 0) {
            finalResult.append("Main Game Loss: $").append(-player1MainWinnings).append("\n");
        }

        if (player1PairPlus > 0) {
            finalResult.append("Pair Plus Winnings: $").append(player1PairPlus).append("\n");
        } else if (playerOne.pairPlusBet > 0) {
            finalResult.append("Pair Plus Loss: $").append(playerOne.pairPlusBet).append("\n");
        }

        finalResult.append("\nPlayer 2:\n");
        finalResult.append(player2Result).append("\n");
        if (player2MainWinnings > 0) {
            finalResult.append("Main Game Winnings: $").append(player2MainWinnings).append("\n");
        } else if (player2MainWinnings < 0) {
            finalResult.append("Main Game Loss: $").append(-player2MainWinnings).append("\n");
        }

        if (player2PairPlus > 0) {
            finalResult.append("Pair Plus Winnings: $").append(player2PairPlus).append("\n");
        } else if (playerTwo.pairPlusBet > 0) {
            finalResult.append("Pair Plus Loss: $").append(playerTwo.pairPlusBet).append("\n");
        }

        // Update displays
        updateWinningsDisplays();
        gameInfoLabel.setText(finalResult.toString());
        playAgainBox.setVisible(true);
        dealButton.setDisable(false);
    }

    private int calculateMainGameWinnings(String result, int anteBet) {
        if (result.contains("Dealer does not qualify")) {
            // Play bet is returned, ante pushes
            return anteBet; // Return the play bet only
        } else if (result.contains("win")) {
            // Player wins 1:1 on both ante and play
            return (anteBet * 4); // Win both bets plus original bets back (anteBet * 2 for each bet)
        } else if (result.contains("lose")) {
            // Lose both ante and play bets
            return -(anteBet * 2); // Indicate loss but don't subtract from total
        } else if (result.contains("tie")) {
            // Bets are returned
            return 0;
        }
        return 0;
    }

    private void updateWinningsDisplays() {
        // Always use green since we're only showing winnings now
        player1TotalWinningsLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-size: 14px; -fx-font-weight: bold;");
        player2TotalWinningsLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-size: 14px; -fx-font-weight: bold;");

        player1TotalWinningsLabel.setText(String.format("Total Winnings: $%d", playerOne.totalWinnings));
        player2TotalWinningsLabel.setText(String.format("Total Winnings: $%d", playerTwo.totalWinnings));
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
    }

    private void showStartScreen() {
        primaryStage.setScene(startScene);
        primaryStage.setTitle("Three Card Poker");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
