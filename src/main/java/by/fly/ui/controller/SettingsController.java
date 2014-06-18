package by.fly.ui.controller;

import by.fly.model.Settings;
import by.fly.service.PrinterService;
import by.fly.service.SettingsService;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Component
public class SettingsController extends AbstractController {

    public ComboBox<String> printerCombo;
    public TextArea itemTypes;

    @Autowired
    private PrinterService printerService;

    @Autowired
    private SettingsService settingsService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        initializeSettingControls();
    }

    private void initializeSettingControls() {
        initializePrinterCombo();
        initializeItemTypes();
    }

    private void initializeItemTypes() {
        itemTypes.setText(settingsService.getItemTypes().stream().collect(Collectors.joining("\n")));
    }

    private void initializePrinterCombo() {
        printerCombo.setItems(FXCollections.observableList(printerService.getAvailablePrinters().keySet().stream().collect(Collectors.toList())));
        final Settings defaultPrinter = settingsService.findOne(Settings.DEFAULT_PRINTER);
        printerCombo.setValue(defaultPrinter != null ? (String) defaultPrinter.getValue() : "");
    }

    public void cancelSettings() {
        initializeSettingControls();
    }


    public void saveSettings() {
        settingsService.save(new Settings(Settings.DEFAULT_PRINTER, printerCombo.getValue()));
        settingsService.save(new Settings(Settings.ITEM_TYPES, Arrays.asList(itemTypes.getText().split("\\n"))));
    }
}
