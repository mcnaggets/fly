package by.fly.ui.controller;

import by.fly.model.OrderItem;
import by.fly.model.OrderStatus;
import by.fly.model.User;
import by.fly.service.OrderService;
import by.fly.service.PrinterService;
import by.fly.service.UserService;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.ResourceBundle;

import static by.fly.ui.UIUtils.DATE_FORMATTER;
import static by.fly.ui.UIUtils.TIME_FORMATTER;
import static javafx.scene.control.Alert.AlertType.WARNING;
import static javafx.scene.control.ButtonType.CLOSE;

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

    @Autowired
    private PrinterService printerService;

    private OrderItem orderItem;

    private User master;

    private ValidationSupport validationSupport;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        initializeMasterBarcodeText();
        initializeHeaderLabels();
        applyValidation();
    }

    public ValidationSupport getValidationSupport() {
        return Optional.ofNullable(validationSupport).orElse(validationSupport = new ValidationSupport());
    }

    private void applyValidation() {
        getValidationSupport().registerValidator(masterBarcodeText, Validator.createEmptyValidator("Мастер должен быть заполнен"));
    }

    public void initializeMasterBarcodeText() {
        masterBarcodeText.textProperty().addListener(e -> {
            String barcode = masterBarcodeText.getText();
            master = userService.findMasterByBarcode(barcode);
            if (master != null) {
                masterNameText.setText(master.getName());
            } else {
                masterNameText.setText("");
            }
        });
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
        closeWindow();
    }

    public void apply() {
        try {
            validateOrderItem();
            populateOrderItem();
            saveOrder();
        } catch (IllegalStateException x) {
            new Alert(WARNING, x.getMessage(), CLOSE).showAndWait();
        }
    }

    private void validateOrderItem() throws IllegalStateException{
        if (master == null) {
            throw new IllegalStateException("Мастер должен быть заполнен");
        }
    }

    public void saveOrder() {
        orderService.save(orderItem);
    }

    public void save() {
        try {
            validateOrderItem();
            populateOrderItem();
            markOrderItemReady();
            saveOrder();
            closeWindow();
        } catch (IllegalStateException x) {
            new Alert(WARNING, x.getMessage(), CLOSE);
        }
    }

    public void closeWindow() {
        getView().getScene().getWindow().hide();
    }

    public void markOrderItemReady() {
        orderItem.setStatus(OrderStatus.READY);
        orderItem.setDeadLine(LocalDateTime.now());
    }

    private void bindOrderItem() {
        printerModelText.setText(orderItem.getPrinterModel());
        clientNameText.setText(orderItem.getClientName());
        workTypeText.setText(orderItem.getWorkTypeMessages("\n"));
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

    public void printReport() throws PrinterException, IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("pdf документы", "*.pdf"));

        File file = fileChooser.showOpenDialog(null);
        printerService.printPDF(file);
    }
}