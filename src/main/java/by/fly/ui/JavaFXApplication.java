package by.fly.ui;

import by.fly.ui.controller.MainController;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class JavaFXApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainController controller = (MainController) SpringFXMLLoader.load("/fxml/main.fxml");
        Scene scene = new Scene((Parent) controller.getView(), 800, 600);
        primaryStage.setTitle("Fly app");
        primaryStage.setScene(scene);
        primaryStage.show();

        final Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setScene(new Scene((Parent) SpringFXMLLoader.load("/fxml/login.fxml").getView()));
//        dialog.show();
    }
}