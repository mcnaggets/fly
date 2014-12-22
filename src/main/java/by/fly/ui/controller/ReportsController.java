package by.fly.ui.controller;

import by.fly.model.OrderItem;
import by.fly.model.WorkType;
import by.fly.model.facet.OrderItemFacets;
import by.fly.service.*;
import by.fly.util.Utils;
import com.mysema.query.BooleanBuilder;
import com.mysema.query.types.expr.BooleanExpression;
import impl.org.controlsfx.autocompletion.AutoCompletionTextFieldBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static by.fly.model.QOrderItem.orderItem;
import static by.fly.ui.UIUtils.DATE_FORMATTER;
import static by.fly.ui.UIUtils.refreshPagination;
import static by.fly.util.Utils.readyOrdersPredicate;
import static by.fly.util.Utils.sortByOrderDeadLine;
import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.Alert.AlertType.WARNING;
import static javafx.scene.control.ButtonType.CLOSE;

@Component
public class ReportsController extends AbstractController {

    public static final int TABLE_PAGE_SIZE = 30;

    private final GetOrdersService service = new GetOrdersService();
    private final AtomicBoolean doRefreshData = new AtomicBoolean(true);

    public DatePicker orderStartDateFilter;
    public DatePicker orderEndDateFilter;
    public TextField clientNameFilter;
    public TextField masterFilter;
    public TextField printerModelFilter;
    public VBox printerTypeFilterContainer;
    private CheckBox[] itemTypeCheckBoxes;
    public CheckBox anyDateFilter;

    public TableView<OrderItem> ordersTable;
    public TableView<OrderItemFacets> ordersFacetTable;
    public TableColumn<OrderItem, String> printerModelColumn;
    public TableColumn<OrderItem, String> workTypeColumn;
    public TableColumn<OrderItem, String> masterColumn;
    public TableColumn<OrderItem, String> deadLineColumn;
    public TableColumn<OrderItem, String> priceColumn;
    public TableColumn<OrderItem, String> printerTypeColumn;

    public TableColumn<OrderItemFacets, String> printerModelFacetColumn;
    public TableColumn<OrderItemFacets, String> workTypeFacetColumn;
    public TableColumn<OrderItemFacets, String> masterFacetColumn;
    public TableColumn<OrderItemFacets, String> deadLineFacetColumn;
    public TableColumn<OrderItemFacets, String> priceFacetColumn;
    public TableColumn<OrderItemFacets, String> printerTypeFacetColumn;

    public ProgressIndicator progressIndicator;
    public Pagination pagination;
    private BooleanBuilder filterPredicate;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private SettingsService settingsService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        initializeTotalsTable();
        initializeFilter();
        initializeColumns();
        applyFilterFieldsAutoCompletion();
        bindService();
        createItemTypeCheckBoxes();
    }

    private void initializeTotalsTable() {
        printerModelFacetColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFacetsAsString(orderItem.printerModel)));
        workTypeFacetColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFacetsAsString(orderItem.workTypes,
                v -> String.format("%d - %s", v.getCount(), WorkType.valueOf(v.getValue()).getMessage()))));
        masterFacetColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFacetsAsString(orderItem.masterName)));
        priceFacetColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getTotalPrice())));
        printerTypeFacetColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFacetsAsString(orderItem.itemType)));
    }

    private void applyFilterFieldsAutoCompletion() {
        new AutoCompletionTextFieldBinding<>(clientNameFilter, provider -> customerService.findCustomerNames(provider.getUserText()));
        new AutoCompletionTextFieldBinding<>(masterFilter, provider -> userService.findUserNames(provider.getUserText()));
        new AutoCompletionTextFieldBinding<>(printerModelFilter, provider -> orderService.findPrinterModels(provider.getUserText()));

    }

    private void initializeDateFilter() {
        anyDateFilter.setOnAction(e -> {
            orderStartDateFilter.setDisable(anyDateFilter.isSelected());
            orderEndDateFilter.setDisable(anyDateFilter.isSelected());
        });
        orderStartDateFilter.setValue(LocalDate.now());
        orderEndDateFilter.setValue(LocalDate.now());
    }

    private void initializeFilter() {
        initializeDateFilter();
    }

    private void initializeColumns() {
        printerTypeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getItemType()));
        printerModelColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrinterModel()));
        masterColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMasterName()));
        workTypeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getWorkTypeMessages("\n")));
        priceColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getPrice())));
        deadLineColumn.setCellValueFactory(data -> new SimpleStringProperty(DATE_FORMATTER.format(data.getValue().getDeadLine())));
    }

    private void bindService() {
        progressIndicator.setMaxSize(150, 150);
        progressIndicator.progressProperty().bind(service.progressProperty());
        progressIndicator.visibleProperty().bind(service.runningProperty());
        ordersTable.itemsProperty().bind(service.valueProperty());
        pagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> service.restart());
        service.setOnRunning(e -> ordersTable.setPlaceholder(new Text("Загрузка...")));
        service.setOnSucceeded(e -> afterDataLoaded());
    }

    private void afterDataLoaded() {
        int totalCount = (int) orderService.count(filterPredicate);
        refreshPagination(pagination, totalCount);
        if (totalCount == 0) {
            ordersTable.setPlaceholder(new Text("Ничего не найдено"));
        }
    }

    public void export(ActionEvent actionEvent) throws IOException {
        if (toMuchExportData()) return;
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setWrapText(true);

        final int rowOffset = createReportHeader(sheet);
        createReportCells(sheet, cellStyle, rowOffset);
        createReportFooter(sheet, rowOffset);
        autoSizeReportColumns(sheet);
        saveReportFile(workbook);
    }

    private void createReportFooter(XSSFSheet sheet, int rowOffset) {

    }

    private boolean toMuchExportData() {
        final long count = orderService.count(filterPredicate);
        if (count > 10_000) {
            new Alert(WARNING, "Слишком много данных для отчёта", CLOSE).showAndWait();
            return true;
        }
        return false;
    }

    private void autoSizeReportColumns(XSSFSheet sheet) {
        IntStream.range(0, ordersTable.getColumns().size()).forEach(sheet::autoSizeColumn);
    }

    private void saveReportFile(XSSFWorkbook workbook) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить отчёт");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Microsoft Excel", "*.xlsx"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                workbook.write(fileOutputStream);
            } catch (IOException x) {
                new Alert(ERROR, x.getMessage(), CLOSE).showAndWait();
            }
        }
    }

    private void createReportCells(XSSFSheet sheet, CellStyle cellStyle, int rowOffset) {
        // XXX: may be performance and memory issues
        final List<OrderItem> orderItems = orderService.findAll(filterPredicate);
        for (int itemIndex = 0; itemIndex < orderItems.size(); itemIndex++) {
            createReportRowCells(sheet.createRow(rowOffset + itemIndex + 1), cellStyle, orderItems.get(itemIndex));
        }
    }

    private void createReportRowCells(Row row, CellStyle cellStyle, OrderItem item) {
        for (int columnIndex = 0; columnIndex < ordersTable.getColumns().size(); columnIndex++) {
            final TableColumn<OrderItem, ?> column = ordersTable.getColumns().get(columnIndex);
            final org.apache.poi.ss.usermodel.Cell cell = row.createCell(columnIndex);
            cell.setCellValue((String) column.getCellObservableValue(item).getValue());
            cell.setCellStyle(cellStyle);
        }
    }

    private int createReportHeader(XSSFSheet sheet) {
        int rowOffset = 0;
        if (!anyDateFilter.isSelected()) {
            sheet.createRow(rowOffset++).createCell(0).setCellValue(String.format("За число c %s по %s",
                    DATE_FORMATTER.format(orderStartDateFilter.getValue()), orderEndDateFilter.getValue().format(DATE_FORMATTER)));
        }
        if (StringUtils.isNotBlank(printerModelFilter.getText())) {
            sheet.createRow(rowOffset++).createCell(0).setCellValue(String.format("Модель принтера %s", printerModelFilter.getText()));
        }
        if (StringUtils.isNotBlank(masterFilter.getText())) {
            sheet.createRow(rowOffset++).createCell(0).setCellValue(String.format("Мастер %s", masterFilter.getText()));
        }
        if (StringUtils.isNotBlank(clientNameFilter.getText())) {
            sheet.createRow(rowOffset++).createCell(0).setCellValue(String.format("Клиент %s", clientNameFilter.getText()));
        }
        final String itemTypes = Arrays.stream(itemTypeCheckBoxes).filter(CheckBox::isSelected)
                .map(cb -> (String) cb.getUserData())
                .collect(Collectors.joining(", "));
        if (StringUtils.isNotBlank(itemTypes)) {
            sheet.createRow(rowOffset++).createCell(0).setCellValue(String.format("Тип(ы) %s", clientNameFilter.getText()));
        }

        return createReportTableHeader(sheet, rowOffset);
    }

    private int createReportTableHeader(XSSFSheet sheet, int rowOffset) {
        Row header = sheet.createRow(rowOffset++);
        for (int i = 0; i < ordersTable.getColumns().size(); i++) {
            header.createCell(i).setCellValue(ordersTable.getColumns().get(i).getText());
        }
        return rowOffset;
    }

    private void createItemTypeCheckBoxes() {
        itemTypeCheckBoxes = settingsService.getItemTypes().stream().map(itemType -> {
            CheckBox checkBox = new CheckBox(itemType);
            checkBox.setUserData(itemType);
            return checkBox;
        }).toArray(CheckBox[]::new);
        printerTypeFilterContainer.getChildren().addAll(itemTypeCheckBoxes);
    }

    private class GetOrdersService extends Service<ObservableList<OrderItem>> {
        @Override
        protected Task<ObservableList<OrderItem>> createTask() {
            return new Task<ObservableList<OrderItem>>() {
                @Override
                protected ObservableList<OrderItem> call() throws Exception {
                    if (!doRefreshData.get()) return FXCollections.emptyObservableList();
                    createFilterPredicate();
                    return getOrderItems();
                }

                private ObservableList<OrderItem> getOrderItems() {
                    Page<OrderItem> orderItems = orderService.findAll(filterPredicate, getPageable());
                    ordersFacetTable.setItems(FXCollections.observableArrayList(reportService.generateFacets()));
                    return FXCollections.observableList(orderItems.getContent());
                }

                private PageRequest getPageable() {
                    return new PageRequest(pagination.getCurrentPageIndex(), TABLE_PAGE_SIZE, sortByOrderDeadLine());
                }
            };
        }
    }

    private void createFilterPredicate() {
        initializeFilterPredicate();
        createDateFilterPredicate();
        createClientNamePredicate();
        createItemTypePredicate();
        createMasterNamePredicate();
        createPrinterModelPredicate();
    }

    private void initializeFilterPredicate() {
        filterPredicate = new BooleanBuilder(readyOrdersPredicate());
    }

    private void createDateFilterPredicate() {
        if (!anyDateFilter.isSelected()) {
            filterPredicate.and(orderItem.deadLine.between(
                    Utils.toDate(orderStartDateFilter.getValue()),
                    Utils.toDate(orderEndDateFilter.getValue().plusDays(1))));
        }
    }

    private void createPrinterModelPredicate() {
        if (StringUtils.isNotBlank(printerModelFilter.getText())) {
            filterPredicate.and(orderItem.printerModel.containsIgnoreCase(printerModelFilter.getText().trim()));
        }
    }

    private void createMasterNamePredicate() {
        if (StringUtils.isNotBlank(masterFilter.getText())) {
            filterPredicate.and(orderItem.masterName.containsIgnoreCase(masterFilter.getText().trim()));
        }
    }

    private void createItemTypePredicate() {
        Arrays.stream(itemTypeCheckBoxes).filter(CheckBox::isSelected)
                .map(cb -> orderItem.itemType.eq((String) cb.getUserData()))
                .reduce(BooleanExpression::or)
                .ifPresent(filterPredicate::and);
    }

    private void createClientNamePredicate() {
        if (StringUtils.isNotBlank(clientNameFilter.getText())) {
            filterPredicate.and(orderItem.clientName.containsIgnoreCase(clientNameFilter.getText().trim()));
        }
    }

    @Override
    public void refresh() {
        service.restart();
    }

}
