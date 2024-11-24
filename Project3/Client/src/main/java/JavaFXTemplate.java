import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class JavaFXTemplate extends Application {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private TextField portField;
    private Label statusLabel;

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        // Port input
        portField = new TextField("8080");
        portField.setMaxWidth(200);
        portField.setPromptText("Enter server port");

        // Connect button
        Button connectButton = new Button("Connect to Server");
        connectButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        // Hit button (initially disabled)
        Button hitButton = new Button("Send 'Hit' to Server");
        hitButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        hitButton.setDisable(true);

        // Status label
        statusLabel = new Label("Not Connected");
        statusLabel.setStyle("-fx-text-fill: red;");

        // Add event handlers
        connectButton.setOnAction(e -> {
            if (connectToServer()) {
                hitButton.setDisable(false);
                statusLabel.setText("Connected to server");
                statusLabel.setStyle("-fx-text-fill: green;");
            }
        });

        hitButton.setOnAction(e -> sendHitMessage());

        root.getChildren().addAll(
            new Label("Poker Client"),
            portField,
            connectButton,
            hitButton,
            statusLabel
        );

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("Poker Client");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Clean up on window close
        primaryStage.setOnCloseRequest(e -> disconnect());
    }

    private boolean connectToServer() {
        try {
            int port = Integer.parseInt(portField.getText());
            socket = new Socket("localhost", port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            return true;
        } catch (Exception e) {
            statusLabel.setText("Connection failed: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            return false;
        }
    }

    private void sendHitMessage() {
        try {
            if (out != null) {
                out.writeObject("Hit"); // Send "Hit" message
                out.flush();
                statusLabel.setText("Sent 'Hit' to server");
            }
        } catch (Exception e) {
            statusLabel.setText("Failed to send message: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void disconnect() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (Exception e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}