package by.fly.ui.controller;

import javafx.scene.Node;

import java.net.URL;
import java.util.ResourceBundle;

public abstract class AbstractController implements Controller {

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