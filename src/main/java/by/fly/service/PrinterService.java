package by.fly.service;

import by.fly.model.Settings;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.print.PrintService;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import static by.fly.model.QSettings.settings;
import static by.fly.model.Settings.DEFAULT_PRINTER;
import static by.fly.util.Utils.getCurrentMachineHardwareAddress;
import static java.awt.print.PrinterJob.lookupPrintServices;
import static java.util.stream.Collectors.toMap;

@Service
public class PrinterService {

    private PrintService currentPrinter;

    @Autowired
    private SettingsService settingsService;

    public Map<String, PrintService> getAvailablePrinters() {
        PrintService[] services = lookupPrintServices();
        return Arrays.stream(services).collect(toMap(PrintService::getName, Function.<PrintService>identity()));
    }

    public void setCurrentPrinter(String name) {
        findPrinterByName(name);
        settingsService.save(new Settings(DEFAULT_PRINTER, name, getCurrentMachineHardwareAddress()));
    }

    private void findPrinterByName(String name) {
        currentPrinter = getAvailablePrinters().get(name);
    }

    public PrintService getCurrentPrinter() {
        if (currentPrinter == null) {
            initializePrinter();
        }
        return currentPrinter;
    }

    public void initializePrinter() {
        Settings printerSetting = settingsService.findOne(settings.name.eq(DEFAULT_PRINTER)
                .and(settings.userData.eq(getCurrentMachineHardwareAddress())));
        if (printerSetting != null) {
            findPrinterByName((String) printerSetting.getValue());
        } else {
            currentPrinter = getAvailablePrinters().values().stream().findFirst().orElse(null);
        }
    }

    public void printPDF(File file) throws PrinterException, IOException {
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setPrintService(getCurrentPrinter());
        PDDocument pdDocument = PDDocument.load(file);
        pdDocument.silentPrint(printerJob);
    }

}
