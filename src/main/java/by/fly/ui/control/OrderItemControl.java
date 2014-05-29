package by.fly.ui.control;

import by.fly.model.OrderItem;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import jfxtras.scene.control.LocalTimeTextField;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class OrderItemControl extends Region {

    private TextField barcodeText;
    private ComboBox<String> printerTypeCombo;
    private TextField printerModelText;
    private LocalTimeTextField deadLineTimePicker;
    private DatePicker deadLineDatePicker;

    public OrderItemControl() {
        super();
        createChildren();
    }

    private void createChildren() {
        HBox root = new HBox();
        root.setSpacing(10);

        barcodeText = new TextField();
        printerTypeCombo = new ComboBox<>(FXCollections.observableArrayList("Лазерный", "Струйный"));
        printerModelText = new TextField();
        deadLineTimePicker = new LocalTimeTextField(LocalTime.now());
        deadLineDatePicker = new DatePicker(LocalDate.now());

        root.getChildren().addAll(
                new Label("Штрихкод:"),
                barcodeText,
                new Label("Тип:"),
                printerTypeCombo,
                new Label("Модель:"),
                printerModelText,
                new Label("Время исполнения:"),
                deadLineDatePicker,
                deadLineTimePicker);

        getChildren().add(root);
    }

    public OrderItem createOrderItem() {
        OrderItem orderItem = new OrderItem(null,
                LocalDateTime.of(deadLineDatePicker.getValue(), deadLineTimePicker.getLocalTime()));
        orderItem.setBarcode(barcodeText.getText());
        orderItem.setPrinterModel(printerModelText.getText());
        orderItem.setPrinterType(printerTypeCombo.getValue());
        return orderItem;
    }

}
