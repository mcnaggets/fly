package by.fly.ui.controller;

import javafx.event.ActionEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

@Component
public class OrdersController {

    public VBox orderTableRegion;
    public AnchorPane createOrderRegion;

    public void createOrder(ActionEvent actionEvent) {
        orderTableRegion.toBack();
        createOrderRegion.toFront();
    }

    public void saveOrder(ActionEvent actionEvent) {
        createOrderRegion.toBack();
        orderTableRegion.toFront();
    }
}
