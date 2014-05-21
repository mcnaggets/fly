package by.fly.ui;

import by.fly.config.ApplicationConfig;
import by.fly.ui.controller.Controller;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;

public class SpringFXMLLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringFXMLLoader.class);

    static final ApplicationContext APPLICATION_CONTEXT = new AnnotationConfigApplicationContext(ApplicationConfig.class);

    public static Controller load(String url) {
        try (InputStream fxmlStream = SpringFXMLLoader.class.getResourceAsStream(url)) {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(ResourceBundle.getBundle("i18n.messages"));
            loader.setControllerFactory(aClass -> APPLICATION_CONTEXT.getBean(aClass));

            Node view = loader.load(fxmlStream);
            Controller controller = loader.getController();
            controller.setView(view);

            return controller;
        } catch (IOException e) {
            LOGGER.error("Can't load resource", e);
            throw new RuntimeException(e);
        }
    }
}