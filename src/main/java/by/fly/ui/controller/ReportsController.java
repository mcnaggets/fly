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
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
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
import java.util.Optional;
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
    private CellStyle boldCellStyle;
    private CellStyle defaultCellStyle;

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
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();

        boldCellStyle = createBoldCellStyle(workbook);
        defaultCellStyle = createDefaultCellStyle(workbook);

        int rowOffset = createReportHeader(sheet);
        rowOffset = createReportCells(sheet, rowOffset);
        createReportFooter(sheet, rowOffset);
        autoSizeReportColumns(sheet);
        saveReportFile(workbook);
    }

    private CellStyle createDefaultCellStyle(HSSFWorkbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        cellStyle.setWrapText(true);
        return cellStyle;
    }

    private void createReportFooter(HSSFSheet sheet, int rowOffset) {
        Row footer = sheet.createRow(rowOffset++);
        for (int i = 0; i < ordersFacetTable.getColumns().size(); i++) {
            final Cell cell = footer.createCell(i);
            cell.setCellStyle(boldCellStyle);
            Optional.ofNullable(ordersFacetTable.getColumns().get(i).getCellObservableValue(0))
                    .ifPresent(v -> cell.setCellValue((String) v.getValue()));
        }
        final HSSFCell cell = sheet.createRow(rowOffset).createCell(0);
        cell.setCellStyle(boldCellStyle);
        cell.setCellValue(String.format("Всего: %s", orderService.count(filterPredicate)));
    }

    private boolean toMuchExportData() {
        final long count = orderService.count(filterPredicate);
        if (count > 10_000) {
            new Alert(WARNING, "Слишком много данных для отчёта", CLOSE).showAndWait();
            return true;
        }
        return false;
    }

    private void autoSizeReportColumns(HSSFSheet sheet) {
        IntStream.range(0, ordersTable.getColumns().size()).forEach(sheet::autoSizeColumn);
    }

    private void saveReportFile(HSSFWorkbook workbook) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить отчёт");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Microsoft Excel", "*.xls"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                workbook.write(fileOutputStream);
            } catch (IOException x) {
                new Alert(ERROR, x.getMessage(), CLOSE).showAndWait();
            }
        }
    }

    private int createReportCells(HSSFSheet sheet, int rowOffset) {
        // XXX: may be performance and memory issues
        for (OrderItem item : orderService.findAll(filterPredicate)) {
            createReportRowCells(sheet.createRow(rowOffset++), item);
        }
        return rowOffset;
    }

    private void createReportRowCells(Row row, OrderItem item) {
        for (int columnIndex = 0; columnIndex < ordersTable.getColumns().size(); columnIndex++) {
            final TableColumn<OrderItem, ?> column = ordersTable.getColumns().get(columnIndex);
            final org.apache.poi.ss.usermodel.Cell cell = row.createCell(columnIndex);
            cell.setCellStyle(defaultCellStyle);
            cell.setCellValue((String) column.getCellObservableValue(item).getValue());
        }
    }

    private int createReportHeader(HSSFSheet sheet) {
        int rowOffset = createReportDateHeader(sheet, 0);
        rowOffset = createReportPrinterModelHeader(sheet, rowOffset);
        rowOffset = createReportMasterHeader(sheet, rowOffset);
        rowOffset = createReportClientNameHeader(sheet, rowOffset);
        rowOffset = createReportItemTypesHeader(sheet, rowOffset);
        rowOffset++;
        return createReportTableHeader(sheet, rowOffset);
    }

    private int createReportFilterHeader(HSSFSheet sheet, int rowOffset, String headerName, String headerValue) {
        if (StringUtils.isNotBlank(headerValue)) {
            final HSSFRow row = sheet.createRow(rowOffset++);
            final HSSFCell cell = row.createCell(0);
            cell.setCellValue(headerName);
            cell.setCellStyle(boldCellStyle);
            row.createCell(1).setCellValue(headerValue);
        }
        return rowOffset;
    }

    private int createReportItemTypesHeader(HSSFSheet sheet, int rowOffset) {
        final String itemTypes = Arrays.stream(itemTypeCheckBoxes).filter(CheckBox::isSelected)
                .map(cb -> (String) cb.getUserData())
                .collect(Collectors.joining(", "));
        return createReportFilterHeader(sheet, rowOffset, "Типы", itemTypes);
    }

    private int createReportClientNameHeader(HSSFSheet sheet, int rowOffset) {
        return createReportFilterHeader(sheet, rowOffset, "Клиент", clientNameFilter.getText());
    }

    private int createReportMasterHeader(HSSFSheet sheet, int rowOffset) {
        return createReportFilterHeader(sheet, rowOffset, "Мастер", masterFilter.getText());
    }

    private int createReportPrinterModelHeader(HSSFSheet sheet, int rowOffset) {
        return createReportFilterHeader(sheet, rowOffset, "Модель принтера", printerModelFilter.getText());
    }

    private int createReportDateHeader(HSSFSheet sheet, int rowOffset) {
        if (!anyDateFilter.isSelected()) {
            return createReportFilterHeader(sheet, rowOffset, "За число", String.format("c %s по %s",
                    DATE_FORMATTER.format(orderStartDateFilter.getValue()), orderEndDateFilter.getValue().format(DATE_FORMATTER)));
        }
        return rowOffset;
    }

    private int createReportTableHeader(HSSFSheet sheet, int rowOffset) {
        Row header = sheet.createRow(rowOffset++);
        for (int i = 0; i < ordersTable.getColumns().size(); i++) {
            final Cell cell = header.createCell(i);
            cell.setCellStyle(boldCellStyle);
            cell.setCellValue(ordersTable.getColumns().get(i).getText());
        }
        return rowOffset;
    }

    private CellStyle createBoldCellStyle(HSSFWorkbook workbook) {
        final CellStyle cellStyle = createDefaultCellStyle(workbook);
        final HSSFFont font = workbook.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        cellStyle.setFont(font);
        return cellStyle;
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
