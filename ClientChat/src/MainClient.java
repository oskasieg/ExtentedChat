import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainClient extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("page/MainPage.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Projekt Java - K.Sieg");
        primaryStage.show();
    }
}
