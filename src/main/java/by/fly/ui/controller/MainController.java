package by.fly.ui.controller;

import by.fly.ui.SpringFXMLLoader;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class MainController extends AbstractController {

    public Hyperlink loginLink;

    public TabPane tabs;

    private Tab organizationTab;
    private Tab dailyOrdersTab;
    private Tab ordersTab;
    private Tab tasksTab;

    private Stage stage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        createTabs();
    }

    private void createTabs() {
        tasksTab = new Tab("Задачи");
        Controller tasksController = SpringFXMLLoader.load("/fxml/tasks.fxml");
        tasksTab.setContent(tasksController.getView());
        tabs.getTabs().add(tasksTab);

        organizationTab = new Tab("Настройки организации");
        organizationTab.setContent(SpringFXMLLoader.load("/fxml/organization.fxml").getView());

        dailyOrdersTab = new Tab("Заказы по дням");
        Controller dailyOrdersController = SpringFXMLLoader.load("/fxml/daily-orders.fxml");
        dailyOrdersTab.setContent(dailyOrdersController.getView());

        ordersTab = new Tab("Заказы");
        Controller ordersController = SpringFXMLLoader.load("/fxml/orders.fxml");
        ordersTab.setContent(ordersController.getView());

        tabs.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == dailyOrdersTab) dailyOrdersController.refresh();
            else if (newValue == ordersTab) ordersController.refresh();
            else if (newValue == tasksTab) tasksController.refresh();
        });
    }

    private void addAdminTabs() {
        tabs.getTabs().add(ordersTab);
        tabs.getTabs().add(dailyOrdersTab);
        tabs.getTabs().add(organizationTab);
    }

    private void removeAdminTabs() {
        tabs.getTabs().remove(ordersTab);
        tabs.getTabs().remove(dailyOrdersTab);
        tabs.getTabs().remove(organizationTab);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }

    public void doLogin() {
        internalLogin();
//        if (isLoggedIn()) {
//            internalLogout();
//        } else {
//            final Stage dialog = new Stage(StageStyle.UTILITY);
//            dialog.initModality(Modality.WINDOW_MODAL);
//            dialog.setMaximized(false);
//            dialog.setResizable(false);
//            dialog.initOwner(getStage());
//            Node view = SpringFXMLLoader.load("/fxml/login.fxml").getView();
//            dialog.setScene(new Scene((Parent) view));
//            dialog.show();
//        }
    }

    private boolean isLoggedIn() {
        return Boolean.TRUE.equals(loginLink.getUserData());
    }

    private void internalLogout() {
        loginLink.setUserData(Boolean.FALSE);
        loginLink.setText("Войти как администратор (через vk.com)");
        removeAdminTabs();
    }

    public void internalLogin() {
        loginLink.setUserData(Boolean.TRUE);
        loginLink.setText("Выйти");
        addAdminTabs();
    }
}
