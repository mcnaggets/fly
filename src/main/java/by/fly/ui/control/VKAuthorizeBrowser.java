package by.fly.ui.control;

import by.fly.config.LogbackConfig;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

public class VKAuthorizeBrowser extends StackPane {

    private final ObjectProperty<EventHandler<ActionEvent>> onLoginSuccess = new SimpleObjectProperty<>();

    private final WebView browser = new WebView();
    private final WebEngine webEngine = browser.getEngine();
    private final ProgressIndicator progress = new ProgressIndicator();

    public VKAuthorizeBrowser() {
        initializeBrowser();
    }

    private void initializeBrowser() {
        // reset cookies
        CookieManager.setDefault(new CookieManager());

        progress.setMaxSize(150, 150);
        browser.setPrefSize(500, 500);
        getChildren().addAll(browser, progress);

        progress.progressProperty().bind(webEngine.getLoadWorker().progressProperty());
        progress.visibleProperty().bind(webEngine.getLoadWorker().runningProperty());

        webEngine.load("https://oauth.vk.com/authorize?client_id=" + LogbackConfig.FLY_APP_VK_ID + "&scope=groups&redirect_uri=https://oauth.vk.com/blank.html&display=mobile&v=5.21&response_type=token");
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> handleBrowserSuccess(newState));
    }

    private void handleBrowserSuccess(Worker.State newState) {
        if (newState == Worker.State.SUCCEEDED) {
            processAuthorization();
        }
    }

    private void processAuthorization() {
        if (authorizationCancelled()) {
            closeWindow();
        } else if (authenticationOk()) {
            fireSuccessLogin(loggedInUserIsAdmin(authorize()));
        }
    }

    private void closeWindow() {
        getScene().getWindow().hide();
    }

    private void fireSuccessLogin(boolean isAdmin) {
        if (onLoginSuccess.get() != null) {
            onLoginSuccess.get().handle(new ActionEvent(isAdmin, this));
        }
    }

    private String authorize() {
        return webEngine.getLocation().substring(webEngine.getLocation().indexOf("#") + 1);
    }

    private boolean authenticationOk() {
        return webEngine.getLocation().contains("oauth.vk.com/blank.html#");
    }

    private boolean authorizationCancelled() {
        return webEngine.getLocation().contains("error");
    }

    private boolean loggedInUserIsAdmin(String authorize) {
        try {
            HttpURLConnection connection = creteConnection(authorize);
            if (connection.getResponseCode() != 200)
                throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
            DBObject response = (DBObject) JSON.parse(new BufferedReader(new InputStreamReader(connection.getInputStream())).lines().collect(Collectors.joining()));
            connection.disconnect();
            return (int) response.get("response") == 1;
        } catch (IOException e) {
            throw new RuntimeException("Can't access VK API", e);
        }
    }

    private HttpURLConnection creteConnection(String authorize) throws IOException {
        URL url = new URL("https://api.vk.com/method/groups.isMember?group_id=" + LogbackConfig.FLY_APP_VK_GROUP_ID + "&extended=0&" + authorize);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        return connection;
    }

    public void setOnLoginSuccess(EventHandler<ActionEvent> onLoginSuccess) {
        this.onLoginSuccess.setValue(onLoginSuccess);
    }
}