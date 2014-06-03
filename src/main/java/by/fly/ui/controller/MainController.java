package by.fly.ui.controller;

import by.fly.ui.SpringFXMLLoader;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class MainController extends AbstractController {

    public TextField userNameField;
    public PasswordField passwordField;
    public GridPane loginPane;
    public Hyperlink loginLink;
    public TabPane tabs;
    public Tab organizationTab;

    private Stage stage;

    public void handleCancelButtonAction(ActionEvent actionEvent) {
        loginPane.getScene().getWindow().hide();
    }

    public void handleSubmitButtonAction(ActionEvent actionEvent) {
        if (loginSuccess()) {
            loginPane.getScene().getWindow().hide();
            internalLogin();
        } // TODO: illegal login handle
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        organizationTab = new Tab("Настройки организации");
        organizationTab.setContent(SpringFXMLLoader.load("/fxml/organization.fxml").getView());
    }

    private boolean loginSuccess() {
        return "admin".equals(userNameField.getText())
                && "admin".equals(passwordField.getText());
    }

    public void doLogin(ActionEvent actionEvent) {
        if (Boolean.TRUE.equals(loginLink.getUserData())) {
            internalLogout();
        } else {
            final Stage dialog = new Stage(StageStyle.TRANSPARENT);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.initOwner(stage);
            dialog.setScene(new Scene((Parent) SpringFXMLLoader.load("/fxml/login.fxml").getView()));
            dialog.show();
        }
    }

    private void internalLogout() {
        loginLink.setUserData(Boolean.FALSE);
        loginLink.setText("Войти");
        tabs.getTabs().remove(organizationTab);
    }

    private void internalLogin() {
        loginLink.setUserData(Boolean.TRUE);
        loginLink.setText("Выйти");
        tabs.getTabs().add(organizationTab);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
