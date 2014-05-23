package by.fly.ui.controller;

import by.fly.model.OrderItem;
import by.fly.repository.OrderItemRepository;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class OrdersController {

    public VBox orderTableRegion;
    public AnchorPane createOrderRegion;
    public Label numberLabel;
    public Label orderDateLabel;

    @Autowired
    private OrderItemRepository orderItemRepository;

    public void createOrder(ActionEvent actionEvent) {
        OrderItem orderItem = new OrderItem(null, null);
        orderItemRepository.save(orderItem);

        numberLabel.setText("Заказ №" + orderItem.getId());
        orderDateLabel.setText(DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now()));

        orderTableRegion.toBack();
        createOrderRegion.toFront();
    }

    public void saveOrder(ActionEvent actionEvent) {
        createOrderRegion.toBack();
        orderTableRegion.toFront();
    }
}
