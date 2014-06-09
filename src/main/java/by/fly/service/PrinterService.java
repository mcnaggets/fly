package by.fly.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;

import javax.print.PrintService;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PrinterService {

    private PrintService currentPrinter;

    public Map<String, PrintService> getAvailablePrinters() {
        PrintService[] services = PrinterJob.lookupPrintServices();
        return Arrays.asList(services).stream().collect(Collectors.toMap(PrintService::getName, Function.<PrintService>identity()));
    }

    public void setCurrentPrinter(String name) {
        currentPrinter = getAvailablePrinters().get(name);
    }

    public PrintService getCurrentPrinter() {
        if (currentPrinter == null) {
            currentPrinter = getAvailablePrinters().values().stream().findFirst().get();
        }
        return currentPrinter;
    }

    public void print(File file) throws PrinterException, IOException {
        PrintService[] services = PrinterJob.lookupPrintServices();
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setPrintService(services[0]);
        PDDocument pdDocument = PDDocument.load(file);
        pdDocument.silentPrint(printerJob);
//        Desktop.getDesktop().print(new File("x:\\work\\docs\\test.docx"));

    }
}
