package by.fly.ui.controller;

import by.fly.ui.control.VKAuthorizeBrowser;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

import static javafx.scene.control.Alert.AlertType.INFORMATION;
import static javafx.scene.control.Alert.AlertType.WARNING;
import static javafx.scene.control.ButtonType.CLOSE;

@Component
public class LoginController extends AbstractController {

    public TextField userNameField;
    public PasswordField passwordField;
    public StackPane loginPane;

    @Autowired
    private MainController mainController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        initializeBrowser();
    }

    private void initializeBrowser() {
        VKAuthorizeBrowser browser = new VKAuthorizeBrowser();
        browser.setOnLoginSuccess(e -> doLogin((Boolean) e.getSource()));
        loginPane.getChildren().add(browser);
    }

    public void handleCancelButtonAction() {
        loginPane.getScene().getWindow().hide();
    }

    public void handleSubmitButtonAction() {
        if (loginSuccess()) {
            doLogin(true);
        } else {
            new Alert(WARNING, "Неверное имя пользователя или пароль", CLOSE).showAndWait();
        }
    }

    private void doLogin(boolean isAdmin) {
        loginPane.getScene().getWindow().hide();
        if (isAdmin) {
            mainController.internalLogin();
        } else {
            new Alert(INFORMATION, "Вы не я вляетесь администратором. Некоторые области будут не видны.", CLOSE).showAndWait();
        }
    }

    private boolean loginSuccess() {
        return "admin".equals(userNameField.getText())
                && "admin".equals(passwordField.getText());
    }

}