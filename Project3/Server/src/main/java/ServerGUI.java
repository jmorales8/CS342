import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ServerGUI {
    private ServerLogic serverLogic;
    private Runnable onServerStart;
    private ListView<String> gameLog;
    private Label clientCountLabel;

    public ServerGUI(ServerLogic serverLogic, Runnable onServerStart) {
        this.serverLogic = serverLogic;
        this.onServerStart = onServerStart;
    }

    public VBox createPortScene() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Poker Server Setup");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField portField = new TextField("8080");
        portField.setMaxWidth(200);
        portField.setPromptText("Enter port number");

        Button startButton = new Button("Start Server");
        startButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        startButton.setOnAction(e -> {
            try {
                int port = Integer.parseInt(portField.getText());
                if (serverLogic.startServer(port)) {
                    onServerStart.run();
                } else {
                    errorLabel.setText("Failed to start server");
                }
            } catch (NumberFormatException ex) {
                errorLabel.setText("Please enter a valid port number");
            }
        });

        root.getChildren().addAll(titleLabel, portField, startButton, errorLabel);
        return root;
    }

    public VBox createGameScene() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        // Server control section
        HBox controlBox = new HBox(10);
        Button stopButton = new Button("Stop Server");
        Button clearButton = new Button("Clear Log");
        stopButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        clearButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        
        clientCountLabel = new Label("Connected Clients: 0");
        controlBox.getChildren().addAll(stopButton, clearButton, clientCountLabel);

        // Game log section
        gameLog = new ListView<>();
        gameLog.setPrefHeight(300);

        stopButton.setOnAction(e -> {
            serverLogic.stopServer();
            updateLog("Server stopped");
        });

        clearButton.setOnAction(e -> gameLog.getItems().clear());

        root.getChildren().addAll(controlBox, gameLog);
        return root;
    }

    public void updateLog(String message) {
        Platform.runLater(() -> gameLog.getItems().add(message));
    }

    public void updateClientCount(int count) {
        Platform.runLater(() -> clientCountLabel.setText("Connected Clients: " + count));
    }
}