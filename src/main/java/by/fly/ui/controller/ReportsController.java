package by.fly.ui.controller;

import by.fly.model.OrderItem;
import by.fly.model.QOrderItem;
import by.fly.service.CustomerService;
import by.fly.service.OrderService;
import by.fly.service.UserService;
import by.fly.util.Utils;
import com.mysema.query.types.expr.BooleanExpression;
import impl.org.controlsfx.autocompletion.AutoCompletionTextFieldBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

import static by.fly.ui.UIUtils.*;
import static by.fly.util.Utils.readyOrdersPredicate;
import static by.fly.util.Utils.sortByOrderDeadLine;
import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY;

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
        initializeTable();
        initializeFilter();
        initializeColumns();
        applyFilterFieldsAutoCompletion();
        bindService();
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
                    Page<OrderItem> orderItems = orderService.findAll(filterPredicate, getPageable());
                    return FXCollections.observableList(orderItems.getContent());
                }

                private PageRequest getPageable() {
                    return new PageRequest(pagination.getCurrentPageIndex(), DEFAULT_PAGE_SIZE, sortByOrderDeadLine());
                }
            };
        }
    }

    private void createDateFilterPredicate() {
        filterPredicate = filterPredicate.and(QOrderItem.orderItem.deadLine.between(
                Utils.toDate(orderStartDateFilter.getValue()),
                Utils.toDate(orderEndDateFilter.getValue().plusDays(1))));
    }

    private void createFilterPredicate() {
        filterPredicate = readyOrdersPredicate();
        createDateFilterPredicate();
        if (StringUtils.isNotBlank(clientNameFilter.getText())) {
            filterPredicate = filterPredicate.and(QOrderItem.orderItem.clientName.containsIgnoreCase(clientNameFilter.getText().trim()));
        }
        if (StringUtils.isNotBlank(printerTypeFilter.getText())) {
            filterPredicate = filterPredicate.and(QOrderItem.orderItem.itemType.containsIgnoreCase(printerTypeFilter.getText().trim()));
        }
        if (StringUtils.isNotBlank(masterFilter.getText())) {
            filterPredicate = filterPredicate.and(QOrderItem.orderItem.masterName.containsIgnoreCase(masterFilter.getText().trim()));
        }
        if (StringUtils.isNotBlank(printerModelFilter.getText())) {
            filterPredicate = filterPredicate.and(QOrderItem.orderItem.printerModel.containsIgnoreCase(printerModelFilter.getText().trim()));
        }
    }

    @Override
    public void refresh() {
        service.restart();
    }

}
