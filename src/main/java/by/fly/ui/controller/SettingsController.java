package by.fly.ui.controller;

import by.fly.model.OrderItem;
import by.fly.model.QOrganization;
import by.fly.model.Settings;
import by.fly.service.OrganizationService;
import by.fly.service.PrinterService;
import by.fly.service.SettingsService;
import by.fly.service.TemplateService;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.web.HTMLEditor;
import org.apache.velocity.VelocityContext;
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
    public HTMLEditor htmlEditor;

    @Autowired
    private PrinterService printerService;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private TemplateService templateService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        initializeSettingControls();
        initializeHtmlEditor();
    }

    private void initializeHtmlEditor() {
        VelocityContext context = new VelocityContext();
        context.put(QOrganization.organization.toString(), organizationService.getRootOrganization());
        context.put("items", Arrays.asList(new OrderItem(1), new OrderItem(2)));
        htmlEditor.setHtmlText(templateService.mergeTemplate("html/template1.html", context));
    }

    private void initializeSettingControls() {
        initializePrinterCombo();
        initializeItemTypes();
    }

    private void initializeItemTypes() {
        itemTypes.setText(settingsService.getItemTypes().stream().collect(Collectors.joining("\n")));
    }

    private void initializePrinterCombo() {
        printerCombo.setItems(FXCollections.observableList(printerService.getAvailablePrinterNames()));
        printerService.getCurrentPrinter().ifPresent(defaultPrinter -> printerCombo.setValue(defaultPrinter.getName()));
    }

    public void cancelSettings() {
        initializeSettingControls();
    }


    public void saveSettings() {
        printerService.setCurrentPrinter(printerCombo.getValue());
        settingsService.save(new Settings(Settings.ITEM_TYPES, Arrays.asList(itemTypes.getText().split("\\n"))));
        htmlEditor.getHtmlText();
    }
}
