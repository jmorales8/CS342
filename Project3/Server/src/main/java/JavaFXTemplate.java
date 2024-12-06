import java.io.IOException;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class JavaFXTemplate extends Application {

    private TextField portField;
    private Button startButton;
    private Button stopButton;
    private Label statusLabel;
    private ListView<String> gameLog;
    private ServerLogic server;
    private Stage primaryStage;
    private int currentPort; // Add this to track the port number

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        this.server = new ServerLogic();
        server.setGUI(this); // Make sure ServerLogic has a reference to this GUI

        showPortInputScene();

        primaryStage.setTitle("Poker Server");
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> {
            if (server != null) {
                server.stopServer();
            }
        });
    }

    private void showPortInputScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        Label titleLabel = new Label("Poker Server Setup");
        titleLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");

        HBox portInput = new HBox(10);
        portInput.setAlignment(Pos.CENTER);
        Label portLabel = new Label("Port Number:");
        portField = new TextField(String.valueOf(currentPort > 0 ? currentPort : 8080));
        portField.setPrefWidth(100);
        portInput.getChildren().addAll(portLabel, portField);

        startButton = new Button("Start Server");
        startButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: red;");

        root.getChildren().addAll(titleLabel, portInput, startButton, statusLabel);

        startButton.setOnAction(e -> {
            try {
                currentPort = Integer.parseInt(portField.getText());
                if (server.startServer(currentPort)) {
                    showGameMonitorScene();
                } else {
                    statusLabel.setText("Failed to start server");
                }
            } catch (NumberFormatException ex) {
                statusLabel.setText("Please enter a valid port number");
            }
        });

        Scene scene = new Scene(root, 700, 700);
        primaryStage.setScene(scene);
    }

    private void showGameMonitorScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        VBox controls = new VBox(10);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(10));

        HBox buttonRow = new HBox(10);
        buttonRow.setAlignment(Pos.CENTER);

        stopButton = new Button("Stop Server");
        Button clearButton = new Button("Clear Log");
        Button testConnectionButton = new Button("Test Connection");

        stopButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        clearButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        testConnectionButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        Label clientsLabel = new Label("Connected Clients: 0");
        server.setClientsLabel(clientsLabel);
        Label portLabel = new Label("Current Port: " + currentPort);

        Label connectionStatus = new Label("");
        connectionStatus.setStyle("-fx-font-weight: bold;");

        buttonRow.getChildren().addAll(stopButton, clearButton, testConnectionButton);
        controls.getChildren().addAll(buttonRow, clientsLabel, portLabel, connectionStatus);
        root.setTop(controls);

        gameLog = new ListView<>();
        gameLog.setPrefHeight(400);
        root.setCenter(gameLog);

        stopButton.setOnAction(e -> {
            server.stopServer();
            showPortInputScene();
        });

        clearButton.setOnAction(e -> gameLog.getItems().clear());

        testConnectionButton.setOnAction(e -> {
            testConnection(connectionStatus);
        });

        Scene scene = new Scene(root, 700, 700);
        primaryStage.setScene(scene);
    }

    private void testConnection(Label statusLabel) {
        new Thread(() -> {
            try {
                Socket testSocket = new Socket("localhost", currentPort); // Use currentPort instead of reading from portField

                Platform.runLater(() -> {
                    statusLabel.setTextFill(Color.GREEN);
                    statusLabel.setText("Successfully connected to server on port " + currentPort);
                    updateLog("Test connection successful on port " + currentPort);
                });

                testSocket.close();

            } catch (IOException ex) {
                Platform.runLater(() -> {
                    statusLabel.setTextFill(Color.RED);
                    statusLabel.setText("Failed to connect: " + ex.getMessage());
                    updateLog("Test connection failed: " + ex.getMessage());
                });
            }
        }).start();
    }

    public void updateLog(String message) {
        if (gameLog != null) {
            Platform.runLater(() -> {
                gameLog.getItems().add(message);
                gameLog.scrollTo(gameLog.getItems().size() - 1);
            });
        }
    }
}