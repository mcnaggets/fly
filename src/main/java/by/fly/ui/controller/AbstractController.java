package by.fly.ui.controller;

import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public abstract class AbstractController implements Controller {

    static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);
    private Node view;
    protected ResourceBundle resourceBundle;

    public Node getView() {
        return view;
    }

    public void setView(Node view) {
        this.view = view;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }
}