package by.fly.ui.controller;

import by.fly.service.PrinterService;
import com.sun.javafx.print.PrintHelper;
import com.sun.prism.j2d.print.J2DPrinter;
import javafx.collections.FXCollections;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.control.ComboBox;
import javafx.scene.web.HTMLEditor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class PrintController extends AbstractController {

    public ComboBox<String> printerCombo;
    public HTMLEditor htmlEditor;

    @Autowired
    private PrinterService printerService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        initializePrinterCombo();
    }

    public void print() {
        htmlEditor.print(PrinterJob.createPrinterJob(getPrinter()));
    }

    public Printer getPrinter() {
        return PrintHelper.createPrinter(new J2DPrinter(printerService.findPrinterByName(printerCombo.getValue())));
    }

    private void initializePrinterCombo() {
        printerCombo.setItems(FXCollections.observableList(printerService.getAvailablePrinterNames()));
        printerService.getCurrentPrinter().ifPresent(defaultPrinter -> printerCombo.setValue(defaultPrinter.getName()));
    }

}
