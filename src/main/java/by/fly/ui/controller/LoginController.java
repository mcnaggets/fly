package by.fly.ui.controller;

import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoginController extends AbstractController {

    public TextField userNameField;
    public PasswordField passwordField;
    public GridPane loginPane;

    @Autowired
    private MainController mainController;

    public void handleCancelButtonAction() {
        loginPane.getScene().getWindow().hide();
    }

    public void handleSubmitButtonAction() {
        if (loginSuccess()) {
            loginPane.getScene().getWindow().hide();
            mainController.internalLogin();
        } // TODO: illegal login handle
    }

    private boolean loginSuccess() {
        return "admin".equals(userNameField.getText())
                && "admin".equals(passwordField.getText());
    }

}
