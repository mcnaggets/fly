package by.fly.ui.controller;

import by.fly.model.OrderItem;
import by.fly.model.QOrderItem;
import by.fly.service.CustomerService;
import by.fly.service.OrderService;
import by.fly.service.UserService;
import com.mysema.query.types.expr.BooleanExpression;
import impl.org.controlsfx.autocompletion.AutoCompletionTextFieldBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

import static by.fly.ui.UIUtils.DEFAULT_PAGE_SIZE;
import static by.fly.ui.UIUtils.TIME_FORMATTER;
import static by.fly.ui.UIUtils.refreshPagination;

@Component
public class ReportsController extends AbstractController {
    private final GetOrdersService service = new GetOrdersService();
    private final AtomicBoolean doRefreshData = new AtomicBoolean(true);
    public DatePicker orderStartDateFilter;
    public DatePicker orderEndDateFilter;
    public TextField clientNameFilter;
    public TextField printerTypeFilter;
    public TextField masterFilter;
    public TextField printerModelFilter;
    public TextField workTypeFilter;
    public TableView<OrderItem> ordersTable;
    public TableColumn<OrderItem, String> printerModelColumn;
    public TableColumn<OrderItem, String> workTypeColumn;
    public TableColumn<OrderItem, String> masterColumn;
    public TableColumn<OrderItem, String> deadLineColumn;
    public TableColumn<OrderItem, String> priceColumn;
    public TableColumn<OrderItem, String> printerTypeColumn;
    public Pagination pagination;
    public ProgressIndicator progressIndicator;
    private BooleanExpression filterPredicate;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        initializeFilter();
        initializeColumns();
        applyFilterFieldsAutoCompletion();
        bindService();
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
        workTypeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getWorkTypeMessages("\n")));
        priceColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getPrice())));
        deadLineColumn.setCellValueFactory(data -> new SimpleStringProperty(TIME_FORMATTER.format(data.getValue().getDeadLine())));
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

    private class GetOrdersService extends Service<ObservableList<OrderItem>> {
        @Override
        protected Task<ObservableList<OrderItem>> createTask() {
            return new Task<ObservableList<OrderItem>>() {
                @Override
                protected ObservableList<OrderItem> call() throws Exception {
                    if (!doRefreshData.get()) return FXCollections.emptyObservableList();
                    createFilterPredicate();
                    Page<OrderItem> orderItems = orderService.findAll(filterPredicate,
                            new PageRequest(pagination.getCurrentPageIndex(), DEFAULT_PAGE_SIZE,
                                    new Sort(Sort.Direction.ASC, QOrderItem.orderItem.deadLine.getMetadata().getName())));
                    return FXCollections.observableList(orderItems.getContent());
                }
            };
        }
    }

    private void createDateFilterPredicate() {
        filterPredicate = filterPredicate.and(QOrderItem.orderItem.deadLine.between(
                Date.from(orderStartDateFilter.getValue().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(orderEndDateFilter.getValue().plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())));
    }

    private void createFilterPredicate() {
        createDateFilterPredicate();
        if (!clientNameFilter.getText().trim().isEmpty()) {
            filterPredicate = filterPredicate.and(QOrderItem.orderItem.clientName.containsIgnoreCase(clientNameFilter.getText().trim()));
        }
        if (!printerTypeFilter.getText().trim().isEmpty()) {
            filterPredicate = filterPredicate.and(QOrderItem.orderItem.itemType.containsIgnoreCase(printerTypeFilter.getText().trim()));
        }
        if (!masterFilter.getText().trim().isEmpty()) {
            filterPredicate = filterPredicate.and(QOrderItem.orderItem.barcode.containsIgnoreCase(masterFilter.getText().trim()));
        }
        if (!printerModelFilter.getText().trim().isEmpty()) {
            filterPredicate = filterPredicate.and(QOrderItem.orderItem.clientPhone.containsIgnoreCase(printerModelFilter.getText().trim()));
        }
    }

}
