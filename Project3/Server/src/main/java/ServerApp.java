import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerApp extends Application {
    private Stage primaryStage;
    private ServerLogic serverLogic;
    private ServerGUI serverGUI;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.serverLogic = new ServerLogic();
        this.serverGUI = new ServerGUI(serverLogic, this::switchToGameScene);

        primaryStage.setTitle("Poker Server");
        primaryStage.setScene(new Scene(serverGUI.createPortScene(), 400, 300));
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> {
            serverLogic.stopServer();
            Platform.exit();
        });
    }

    public void switchToGameScene() {
        Scene gameScene = new Scene(serverGUI.createGameScene(), 600, 400);
        primaryStage.setScene(gameScene);
        primaryStage.sizeToScene();
    }

    public static void main(String[] args) {
        launch(args);
    }
}