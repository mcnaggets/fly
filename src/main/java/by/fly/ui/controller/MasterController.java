package by.fly.ui.controller;

import by.fly.model.OrderItem;
import by.fly.model.OrderStatus;
import by.fly.model.User;
import by.fly.service.OrderService;
import by.fly.service.UserService;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

import static by.fly.ui.UIUtils.DATE_FORMATTER;
import static by.fly.ui.UIUtils.TIME_FORMATTER;

@Component
public class MasterController extends AbstractController {

    public Label currentDateLabel;
    public Label currentTimeLabel;

    public Text printerModelText;
    public Text clientNameText;
    public Text workTypeText;
    public CheckBox testCheckBox;
    public TextField masterBarcodeText;
    public Text masterNameText;
    public Text barcodeText;
    public TextField additionalWorkText;
    public TextArea descriptionArea;
    public Button saveButton;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    private OrderItem orderItem;
    private User master;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        masterBarcodeText.textProperty().addListener(e -> {
            String barcode = masterBarcodeText.getText();
            master = userService.findMasterByBarcode(barcode);
            if (master != null) {
                masterNameText.setText(master.getName());
            } else {
                masterNameText.setText("");
            }
        });

        initializeHeaderLabels();
    }

    private void initializeHeaderLabels() {
        LocalDateTime now = LocalDateTime.now();
        currentDateLabel.setText(DATE_FORMATTER.format(now));
        currentTimeLabel.setText(TIME_FORMATTER.format(now));
    }

    public void setOrderItem(OrderItem orderItem) {
        this.orderItem = orderItem;
        bindOrderItem();
    }

    public void cancel() {
        getView().getScene().getWindow().hide();
    }

    public void apply() {
        populateOrderItem();
        orderService.save(orderItem);
    }

    public void save() {
        populateOrderItem();
        orderItem.setStatus(OrderStatus.READY);
        orderService.save(orderItem);
        getView().getScene().getWindow().hide();
    }

    private void bindOrderItem() {
        printerModelText.setText(orderItem.getPrinterModel());
        clientNameText.setText(orderItem.getClientName());
        workTypeText.setText(orderItem.getWorkType().getMessage());
        descriptionArea.setText(orderItem.getDescription());

        if (orderItem.getMaster() != null) {
            masterNameText.setText(orderItem.getMaster().getName());
            masterBarcodeText.setText(orderItem.getMaster().getBarcode());
        }

        additionalWorkText.setText(orderItem.getAdditionalWork());

        testCheckBox.setSelected(orderItem.isTest());

        barcodeText.setText("Штрихкод: " + orderItem.getBarcode());

        if (orderItem.getStatus() == OrderStatus.IN_PROGRESS) {
            saveButton.setText("Готов");
            saveButton.setStyle("-fx-background-color: palegreen;");
        } else if (orderItem.getStatus() == OrderStatus.READY) {
            saveButton.setText("Оплачен");
            saveButton.setStyle("-fx-background-color: orange;");
        }
    }

    private void populateOrderItem() {
        orderItem.setDescription(descriptionArea.getText());
        orderItem.setTest(testCheckBox.isSelected());
        orderItem.setMaster(master);
        orderItem.setAdditionalWork(additionalWorkText.getText());
    }
}