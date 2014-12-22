package by.fly.ui.controller;

import by.fly.model.OrderItem;
import by.fly.model.QOrderItem;
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
import org.apache.poi.ss.usermodel.Row;
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

import static by.fly.ui.UIUtils.DATE_FORMATTER;
import static by.fly.ui.UIUtils.refreshPagination;
import static by.fly.util.Utils.readyOrdersPredicate;
import static by.fly.util.Utils.sortByOrderDeadLine;
import static javafx.scene.control.Alert.AlertType.WARNING;
import static javafx.scene.control.ButtonType.CLOSE;
import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY;

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
        initializeTable();
        initializeTotalsTable();
        initializeFilter();
        initializeColumns();
        applyFilterFieldsAutoCompletion();
        bindService();
        createItemTypeCheckBoxes();
    }

    private void initializeTotalsTable() {
        ordersFacetTable.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        printerModelFacetColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFacetsAsString(QOrderItem.orderItem.printerModel, "\n")));
        workTypeFacetColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFacetsAsString(QOrderItem.orderItem.workTypes, "\n")));
        masterFacetColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFacetsAsString(QOrderItem.orderItem.masterName, "\n")));
        priceFacetColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getTotalPrice())));
        printerTypeFacetColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFacetsAsString(QOrderItem.orderItem.itemType, "\n")));
    }

    private void initializeTable() {
        ordersTable.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
    }

    private void applyFilterFieldsAutoCompletion() {
        new AutoCompletionTextFieldBinding<>(clientNameFilter, provider -> customerService.findCustomerNames(provider.getUserText()));
        new AutoCompletionTextFieldBinding<>(masterFilter, provider -> userService.findUserNames(provider.getUserText()));
        new AutoCompletionTextFieldBinding<>(printerModelFilter, provider -> orderService.findPrinterModels(provider.getUserText()));

    }

    private void initializeDateFilter() {
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
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        createReportHeader(sheet);
        createReportCells(sheet);
        autoSizeReportColumns(sheet);
        saveReportFile(workbook);
    }

    private void autoSizeReportColumns(XSSFSheet sheet) {
        for (int columnIndex = 0; columnIndex < sheet.getRow(0).getLastCellNum(); columnIndex++) {
            sheet.autoSizeColumn(columnIndex);
        }
    }

    private void saveReportFile(XSSFWorkbook workbook) throws IOException {
        final long count = orderService.count(filterPredicate);
        if (count > 10_000) {
            new Alert(WARNING, "Слишком много данных для отчёта", CLOSE).showAndWait();
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить отчёт");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Microsoft Excel", "*.xlsx"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                workbook.write(fileOutputStream);
            }
        }
    }

    private void createReportCells(XSSFSheet sheet) {
        // XXX: may be performance and memory issues
        final List<OrderItem> orderItems = orderService.findAll(filterPredicate);
        for (int itemIndex = 0; itemIndex < orderItems.size(); itemIndex++) {
            createReportRowCells(sheet.createRow(itemIndex + 1), orderItems.get(itemIndex));
        }
    }

    private void createReportRowCells(Row row, OrderItem item) {
        for (int columnIndex = 0; columnIndex < ordersTable.getColumns().size(); columnIndex++) {
            final TableColumn<OrderItem, ?> column = ordersTable.getColumns().get(columnIndex);
            row.createCell(columnIndex).setCellValue((String) column.getCellObservableValue(item).getValue());
        }
    }

    private void createReportHeader(XSSFSheet sheet) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < ordersTable.getColumns().size(); i++) {
            header.createCell(i).setCellValue(ordersTable.getColumns().get(i).getText());
        }
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
        filterPredicate.and(QOrderItem.orderItem.deadLine.between(
                Utils.toDate(orderStartDateFilter.getValue()),
                Utils.toDate(orderEndDateFilter.getValue().plusDays(1))));
    }

    private void createPrinterModelPredicate() {
        if (StringUtils.isNotBlank(printerModelFilter.getText())) {
            filterPredicate.and(QOrderItem.orderItem.printerModel.containsIgnoreCase(printerModelFilter.getText().trim()));
        }
    }

    private void createMasterNamePredicate() {
        if (StringUtils.isNotBlank(masterFilter.getText())) {
            filterPredicate.and(QOrderItem.orderItem.masterName.containsIgnoreCase(masterFilter.getText().trim()));
        }
    }

    private void createItemTypePredicate() {
        Arrays.stream(itemTypeCheckBoxes).filter(CheckBox::isSelected)
                .map(cb -> QOrderItem.orderItem.itemType.eq((String) cb.getUserData()))
                .reduce(BooleanExpression::or)
                .ifPresent(filterPredicate::and);
    }

    private void createClientNamePredicate() {
        if (StringUtils.isNotBlank(clientNameFilter.getText())) {
            filterPredicate.and(QOrderItem.orderItem.clientName.containsIgnoreCase(clientNameFilter.getText().trim()));
        }
    }

    @Override
    public void refresh() {
        service.restart();
    }

}
