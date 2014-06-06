package by.fly.ui.controller;

import by.fly.model.OrderItem;
import by.fly.service.OrderService;
import by.fly.ui.SpringFXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class BarcodeReaderController extends AbstractController {

    public TextField barcodeText;

    @Autowired
    private OrderService orderService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        barcodeText.textProperty().addListener(e -> {
            String barcode = barcodeText.getText();
            if (barcode == null || barcode.isEmpty()) return;
            OrderItem orderItem = orderService.findLastItemByBarcode(barcode);
            if (orderItem != null) {
                barcodeText.textProperty().setValue("");
                final Stage dialog = new Stage(StageStyle.UTILITY);
                dialog.initModality(Modality.WINDOW_MODAL);
                dialog.setMaximized(false);
                dialog.setResizable(false);
                dialog.initOwner(barcodeText.getScene().getWindow());
                MasterController masterController = (MasterController) SpringFXMLLoader.load("/fxml/master.fxml");
                masterController.setOrderItem(orderItem);
                dialog.setScene(new Scene((Parent) masterController.getView()));
                dialog.show();
            }
        });

    }
}
