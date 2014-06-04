package by.fly.ui.controller;

import by.fly.model.Customer;
import by.fly.model.OrderItem;
import by.fly.model.OrderStatus;
import by.fly.model.QOrderItem;
import by.fly.service.CustomerService;
import by.fly.service.OrderService;
import by.fly.ui.control.AutoCompleteTextField;
import by.fly.ui.control.OrderItemControl;
import com.mysema.query.types.expr.BooleanExpression;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.ResourceBundle;

import static by.fly.ui.UIUtils.*;

@Component
public class OrdersController extends AbstractController {

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
    public TableView ordersTable;

    public VBox orderItems;
    public AutoCompleteTextField<String> clientPhoneText;
    public AutoCompleteTextField<String> clientNameText;

    public TextField totalPriceText;
    public Label orderCodeLabel;

    public Button inProgressButton;
    public Button readyButton;

    public DatePicker orderDateFilter;
    public TextField orderCodeFilter;
    public TextField orderBarcodeFilter;
    public TextField clientNameFilter;
    public TextField clientPhoneFilter;
    public CheckBox anyDateFilter;

    private GetOrdersService service = new GetOrdersService();

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private BeanFactory beanFactory;

    private BooleanExpression filterPredicate;

    private long orderNumber;

    public void createOrder(ActionEvent actionEvent) {
        orderItems.getChildren().clear();
        orderItems.getChildren().add(createOrderItemControl());

        orderNumber = orderService.getNexOrderNumber();
        orderNumberLabel.setText("Заказ №" + orderNumber);
        orderCodeLabel.setText(OrderItem.ORDER_CODE_PREFIX + orderNumber);
        orderDateLabel.setText(DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now()));

        clientNameText.setText("");
        clientPhoneText.setText("");

        orderTableRegion.toBack();
        createOrderRegion.toFront();
    }

    public void saveOrder(ActionEvent actionEvent) {
        createOrderRegion.toBack();
        orderTableRegion.toFront();

        orderItems.getChildren().forEach(node -> {
            OrderItem orderItem = ((OrderItemControl) node).createOrderItem();
            if (actionEvent.getTarget() == inProgressButton) {
                orderItem.setStatus(OrderStatus.IN_PROGRESS);
            } else if (actionEvent.getTarget() == readyButton) {
                orderItem.setStatus(OrderStatus.READY);
            }
            orderItem.setOrderNumber(orderNumber);
            String clientName = clientNameText.getText();
            String clientPhone = clientPhoneText.getText();
            Customer customer = customerService.findByNameAndPhone(clientName, clientPhone);
            if (customer == null) {
                customer = new Customer(clientName, clientPhone);
                customerService.save(customer);
            }
            orderItem.setCustomer(customer);
            orderService.save(orderItem);
        });

        service.restart();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        initializeFilter();

        clientNameText.setItems(FXCollections.observableList(customerService.findCustomerNames()));
        clientPhoneText.setItems(FXCollections.observableList(customerService.findCustomerPhones()));

        initializeColumns();
        bindService();
    }

    private void initializeFilter() {
        orderDateFilter.setValue(LocalDate.now());
        orderDateFilter.setOnAction(e -> service.restart());
        anyDateFilter.setOnAction(e -> {
            orderDateFilter.setDisable(anyDateFilter.isSelected());
            service.restart();
        });

        orderCodeFilter.textProperty().addListener(e -> service.restart());
        orderBarcodeFilter.textProperty().addListener(e -> service.restart());
        clientNameFilter.textProperty().addListener(e -> service.restart());
        clientPhoneFilter.textProperty().addListener(e -> service.restart());
    }

    private void bindService() {
        progressIndicator.setMaxSize(150, 150);
        progressIndicator.progressProperty().bind(service.progressProperty());
        progressIndicator.visibleProperty().bind(service.runningProperty());
        ordersTable.itemsProperty().bind(service.valueProperty());
        pagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> service.restart());
        service.start();
        service.setOnSucceeded(e -> updatePagination());
    }

    private void initializeColumns() {
        numberColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(getRowIndex(pagination, data))));
        orderNumberColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrderCode()));
        barCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBarcode()));
        printerTypeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrinterType().getMessage()));
        printerModelColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrinterModel()));
        workTypeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getWorkType().getMessage()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().getMessage()));
        priceColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getPrice())));
        clientNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getClientName()));
        clientPhoneColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getClientPhone()));
        createdAtColumn.setCellValueFactory(data -> new SimpleStringProperty(TIME_FORMATTER.format(data.getValue().getCreatedAt())));
        deadLineColumn.setCellValueFactory(data -> new SimpleStringProperty(TIME_FORMATTER.format(data.getValue().getDeadLine())));
    }

    public void addOrderItem(ActionEvent actionEvent) {
        orderItems.getChildren().add(createOrderItemControl());
    }

    private OrderItemControl createOrderItemControl() {
        OrderItemControl orderItemControl = beanFactory.getBean(OrderItemControl.class);
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

    private double calculateTotalPrice() {
        return orderItems.getChildren().stream().mapToDouble(node -> ((OrderItemControl) node).getPrice()).sum();
    }

    public void cancelOrder(ActionEvent actionEvent) {
        createOrderRegion.toBack();
        orderTableRegion.toFront();
    }

    private class GetOrdersService extends Service<ObservableList<OrderItem>> {
        @Override
        protected Task<ObservableList<OrderItem>> createTask() {
            return new Task<ObservableList<OrderItem>>() {
                @Override
                protected ObservableList<OrderItem> call() throws Exception {
                    LocalDate filterDate = orderDateFilter.getValue();
                    filterPredicate = QOrderItem.orderItem.orderCode.isNotNull();
                    if (!anyDateFilter.isSelected()) {
                        filterPredicate = filterPredicate.and(QOrderItem.orderItem.deadLine.between(
                                Date.from(filterDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()),
                                Date.from(filterDate.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())));
                    }
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
                    Page<OrderItem> orderItems = orderService.findAll(filterPredicate,
                            new PageRequest(pagination.getCurrentPageIndex(), DEFAULT_PAGE_SIZE)
                    );
                    return FXCollections.observableList(orderItems.getContent());
                }
            };
        }
    }

    private void updatePagination() {
        int totalCount = (int) orderService.count(filterPredicate);
        refreshPagination(pagination, totalCount);
    }
}
