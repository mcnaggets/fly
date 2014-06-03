package by.fly.ui.controller;

import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableView;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class DailyOrdersController extends AbstractController {

    public TableView dailyOrdersTable;

    public ProgressIndicator progressIndicator;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
    }

}
