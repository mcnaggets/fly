package by.fly.ui;

import by.fly.ui.controller.MainController;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaFXApplication extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaFXApplication.class);

    public static void main(String[] args) throws Exception{
        try {
            launch(args);
        } catch (Exception x) {
            LOGGER.error(x.getMessage(), x);
            throw x;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        MainController controller = (MainController) SpringFXMLLoader.load("/fxml/main.fxml");
        Scene scene = new Scene((Parent) controller.getView(), 1000, 750);
        primaryStage.setTitle(SpringFXMLLoader.APPLICATION_CONTEXT.getEnvironment().getProperty("application.name"));
        primaryStage.setScene(scene);
        primaryStage.show();

        final Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setScene(new Scene((Parent) SpringFXMLLoader.load("/fxml/login.fxml").getView()));
//        dialog.show();
    }
}