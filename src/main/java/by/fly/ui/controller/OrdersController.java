package by.fly.ui.controller;

import by.fly.model.Customer;
import by.fly.model.OrderItem;
import by.fly.model.OrderStatus;
import by.fly.model.QOrderItem;
import by.fly.service.CustomerService;
import by.fly.service.OrderService;
import by.fly.service.PrinterService;
import by.fly.ui.control.OrderItemControl;
import com.mysema.query.types.expr.BooleanExpression;
import impl.org.controlsfx.autocompletion.AutoCompletionTextFieldBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import org.controlsfx.dialog.Dialogs;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

import static by.fly.ui.UIUtils.*;
import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY;

@Component
public class OrdersController extends AbstractController {

    private final GetOrdersService service = new GetOrdersService();
    private final AtomicBoolean doRefreshData = new AtomicBoolean(true);
    public Region orderTableRegion;
    public Region createOrderRegion;
    public Label orderNumberLabel;
    public Label orderDateLabel;
    public TableColumn<OrderItem, String> createdAtColumn;
    public TableColumn<OrderItem, String> numberColumn;
    public TableColumn<OrderItem, String> printerTypeColumn;
    public TableColumn<OrderItem, String> orderNumberColumn;
    public TableColumn<OrderItem, String> barCodeColumn;
    public TableColumn<OrderItem, String> workTypeColumn;
    public TableColumn<OrderItem, String> printerModelColumn;
    public TableColumn<OrderItem, String> clientNameColumn;
    public TableColumn<OrderItem, String> clientPhoneColumn;
    public TableColumn<OrderItem, String> deadLineColumn;
    public TableColumn<OrderItem, String> statusColumn;
    public TableColumn<OrderItem, String> priceColumn;
    public Pagination pagination;
    public ProgressIndicator progressIndicator;
    public TableView<OrderItem> ordersTable;
    public ListView<OrderItemControl> orderItems;
    public TextField clientPhoneText;
    public TextField clientNameText;
    public TextField totalPriceText;
    public Label orderCodeLabel;
    public Button inProgressButton;
    public Button paidButton;
    public Button addOrderItemButton;
    public Button saveButton;
    public DatePicker orderDateFilter;
    public TextField orderCodeFilter;
    public TextField orderBarcodeFilter;
    public TextField clientNameFilter;
    public TextField clientPhoneFilter;
    public CheckBox anyDateFilter;
    @Autowired
    private OrderService orderService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private BeanFactory beanFactory;
    @Autowired
    private PrinterService printerService;
    private BooleanExpression filterPredicate;
    private long orderNumber;
    private AutoCompletionTextFieldBinding<String> clientNameAutoCompletionBinding;
    private AutoCompletionTextFieldBinding<String> clientPhoneAutoCompletionBinding;

    private ValidationSupport validationSupport = new ValidationSupport();

    public void addOrderItem() {
        orderItems.getItems().add(createOrderItemControl(Optional.empty()));
    }

    private void afterDataLoaded() {
        int totalCount = (int) orderService.count(filterPredicate);
        refreshPagination(pagination, totalCount);
        if (totalCount == 0) {
            ordersTable.setPlaceholder(new Text("Ничего не найдено"));
        }
    }

    private void applyClientFieldsAutoCompletion() {
        clientNameAutoCompletionBinding = new AutoCompletionTextFieldBinding<>(clientNameText, provider -> customerService.findCustomerNames(provider.getUserText()));
        clientPhoneAutoCompletionBinding = new AutoCompletionTextFieldBinding<>(clientPhoneText, provider -> customerService.findCustomerNames(provider.getUserText()));
    }

    private void bindButtonsForNewOrder() {
        addOrderItemButton.setDisable(false);
        inProgressButton.setVisible(true);
        paidButton.setVisible(false);
        saveButton.setVisible(false);
    }

    private void bindButtonsForOrderItem(OrderItem orderItem) {
        addOrderItemButton.setDisable(true);
        inProgressButton.setVisible(false);
        paidButton.setVisible(orderItem.getStatus() == OrderStatus.READY);
        saveButton.setVisible(orderItem.getStatus() != OrderStatus.READY);
    }

    private void bindNewOrderItem() {
        orderNumber = orderService.getNexOrderNumber();
        orderNumberLabel.setText("Заказ №" + orderNumber);
        orderCodeLabel.setText(OrderItem.ORDER_CODE_PREFIX + orderNumber);
        orderDateLabel.setText(DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now()));

        disposeClientFieldsAutoCompletion();
        clientNameText.setText("");
        clientPhoneText.setText("");
        applyClientFieldsAutoCompletion();

        bindButtonsForNewOrder();
    }

    private void bindPresentedOrderItem(OrderItem orderItem) {
        orderNumberLabel.setText("Заказ №" + orderItem.getOrderNumber());
        orderCodeLabel.setText(orderItem.getOrderCode());
        orderDateLabel.setText(DateTimeFormatter.ISO_LOCAL_DATE.format(orderItem.getCreatedAt()));

        disposeClientFieldsAutoCompletion();
        clientNameText.setText(orderItem.getClientName());
        clientPhoneText.setText(orderItem.getClientPhone());
        applyClientFieldsAutoCompletion();

        totalPriceText.setText(String.valueOf(orderItem.getPrice()));

        bindButtonsForOrderItem(orderItem);
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

    private double calculateTotalPrice() {
        return orderItems.getItems().stream().mapToDouble(OrderItemControl::getPrice).sum();
    }

    public void cancelOrder() {
        showOrderTable();
    }

    private void clearFilter() {
        doRefreshData.set(false);
        orderDateFilter.setValue(LocalDate.now());
        orderDateFilter.setDisable(false);
        anyDateFilter.setSelected(false);
        clientNameFilter.setText("");
        clientPhoneFilter.setText("");
        orderBarcodeFilter.setText("");
        orderCodeFilter.setText("");
        doRefreshData.set(true);
    }

    private void createDateFilterPredicate() {
        LocalDate filterDate = orderDateFilter.getValue();
        filterPredicate = QOrderItem.orderItem.orderCode.isNotNull();
        if (!anyDateFilter.isSelected()) {
            filterPredicate = filterPredicate.and(QOrderItem.orderItem.deadLine.between(
                    Date.from(filterDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()),
                    Date.from(filterDate.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())));
        }
    }

    private void createFilterPredicate() {
        createDateFilterPredicate();
        if (!orderCodeFilter.getText().trim().isEmpty()) {
            filterPredicate = filterPredicate.and(QOrderItem.orderItem.orderCode.containsIgnoreCase(orderCodeFilter.getText().trim()));
        }
        if (!orderBarcodeFilter.getText().trim().isEmpty()) {
            filterPredicate = filterPredicate.and(QOrderItem.orderItem.barcode.containsIgnoreCase(orderBarcodeFilter.getText().trim()));
        }
        if (!clientNameFilter.getText().trim().isEmpty()) {
            filterPredicate = filterPredicate.and(QOrderItem.orderItem.clientName.containsIgnoreCase(clientNameFilter.getText().trim()));
        }
        if (!clientPhoneFilter.getText().trim().isEmpty()) {
            filterPredicate = filterPredicate.and(QOrderItem.orderItem.clientPhone.containsIgnoreCase(clientPhoneFilter.getText().trim()));
        }
    }

    public void createNewOrder() {
        showOrderUI(Optional.empty());
    }

    private OrderItemControl createOrderItemControl(Optional<OrderItem> orderItemOptional) {
        OrderItemControl orderItemControl = (OrderItemControl) beanFactory.getBean(OrderItemControl.NAME, orderItemOptional, Optional.of(validationSupport));
        orderItemControl.initialize();
        orderItemControl.setOnPriceChanged(e -> totalPriceText.setText(String.valueOf(calculateTotalPrice())));
        orderItemControl.setOnBarcodeChanged(e -> {
            String barcode = (String) e.getSource();
            OrderItem item = orderService.findLastItemByBarcode(barcode);
            clientNameText.setText(item.getCustomer().getName());
            clientPhoneText.setText(item.getCustomer().getPhone());
        });
        return orderItemControl;
    }

    private void disposeClientFieldsAutoCompletion() {
        clientNameAutoCompletionBinding.dispose();
        clientPhoneAutoCompletionBinding.dispose();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        initializeFilter();
        initializeColumns();
        initializeTable();
        applyClientFieldsAutoCompletion();
        applyValidation();
        bindService();
    }

    private void applyValidation() {
        validationSupport.registerValidator(clientNameText, Validator.createEmptyValidator("Имя клиента должно быть заполено"));
        validationSupport.registerValidator(clientPhoneText, Validator.createEmptyValidator("Телефон клиента должно быть заполен"));
    }

    private void initializeClientFilter() {
        clientNameFilter.textProperty().addListener(e -> service.restart());
        new AutoCompletionTextFieldBinding<>(clientNameFilter, provider -> customerService.findCustomerNames(provider.getUserText()));
        clientPhoneFilter.textProperty().addListener(e -> service.restart());
        new AutoCompletionTextFieldBinding<>(clientPhoneFilter, provider -> customerService.findCustomerPhones(provider.getUserText()));
    }

    private void initializeColumns() {
        numberColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(getRowIndex(pagination, data))));
        orderNumberColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrderCode()));
        barCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBarcode()));
        printerTypeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getItemType()));
        printerModelColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrinterModel()));
        workTypeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getWorkTypeMessages("\n")));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().getMessage()));
        priceColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getPrice())));
        clientNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getClientName()));
        clientPhoneColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getClientPhone()));
        createdAtColumn.setCellValueFactory(data -> new SimpleStringProperty(TIME_FORMATTER.format(data.getValue().getCreatedAt())));
        deadLineColumn.setCellValueFactory(data -> new SimpleStringProperty(TIME_FORMATTER.format(data.getValue().getDeadLine())));
    }

    private void initializeDateFilter() {
        orderDateFilter.setValue(LocalDate.now());
        orderDateFilter.setOnAction(e -> service.restart());
        anyDateFilter.setOnAction(e -> {
            orderDateFilter.setDisable(anyDateFilter.isSelected());
            service.restart();
        });
    }

    private void initializeFilter() {
        initializeDateFilter();
        initializeOrderFilter();
        initializeClientFilter();
    }

    private void initializeOrderFilter() {
        orderCodeFilter.textProperty().addListener(e -> service.restart());
        orderBarcodeFilter.textProperty().addListener(e -> service.restart());
    }

    private void initializeTable() {
        ordersTable.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        ordersTable.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                OrderItem orderItem = ordersTable.getSelectionModel().getSelectedItem();
                if (mouseEvent.getClickCount() == 2 && orderItem != null) {
                    showOrderUI(Optional.of(orderItem));
                }
            }
        });
    }

    public void printTicket() throws PrinterException, IOException {
        printerService.printPDF(new File("x:\\work\\docs\\test.pdf"));
    }

    @Override
    public void refresh() {
        service.restart();
    }

    private Customer saveCustomer() {
        String clientName = clientNameText.getText();
        String clientPhone = clientPhoneText.getText();
        Customer customer = customerService.findByNameAndPhone(clientName, clientPhone);
        if (customer == null) {
            customer = new Customer(clientName, clientPhone);
            customerService.save(customer);
        }
        return customer;
    }

    public void saveOrder() {
        try {
            validateOrder();
            showOrderTable();
            saveOrderItems(saveCustomer());
            clearFilter();
            refresh();
        } catch (IllegalStateException x) {
            Dialogs.create().owner(orderTableRegion.getScene().getWindow()).title("Предупреждение").message(x.getMessage()).showWarning();
        }
    }

    private void saveOrderItems(Customer customer) {
        orderItems.getItems().forEach(node -> {
            OrderItem orderItem = node.getOrderItem();
            setOrderItemStatus(orderItem);
            orderItem.setOrderNumber(orderNumber);
            orderItem.setCustomer(customer);
            orderService.save(orderItem);
        });
    }

    private void setOrderItemStatus(OrderItem orderItem) {
        if (orderItem.getStatus() == OrderStatus.CREATED) {
            orderItem.setStatus(OrderStatus.IN_PROGRESS);
        } else if (orderItem.getStatus() == OrderStatus.READY) {
            orderItem.setStatus(OrderStatus.PAID);
        }
    }

    private void showOrderTable() {
        createOrderRegion.toBack();
        orderTableRegion.toFront();
    }

    private void showOrderUI(Optional<OrderItem> orderItemOptional) {
        orderItems.getItems().clear();
        orderItems.getItems().add(createOrderItemControl(orderItemOptional));

        if (orderItemOptional.isPresent()) {
            bindPresentedOrderItem(orderItemOptional.get());
        } else {
            bindNewOrderItem();
        }

        orderTableRegion.toBack();
        createOrderRegion.toFront();
    }

    private void validateOrder() throws IllegalStateException{
        orderItems.getItems().stream().forEach(OrderItemControl::validate);
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

}
