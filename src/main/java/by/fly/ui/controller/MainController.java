package by.fly.ui.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.springframework.stereotype.Component;

@Component
public class MainController extends AbstractController {

    public TextField userNameField;
    public PasswordField passwordField;
    public GridPane loginPane;

    public void handleCancelButtonAction(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void handleSubmitButtonAction(ActionEvent actionEvent) {
        if ("admin".equals(userNameField.getText())
                && "admin".equals(passwordField.getText())) {
            loginPane.getScene().getWindow().hide();
        }
    }

    public void showOrganizationTab(Event event) {

    }
}
