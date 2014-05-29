package by.fly.ui.controller;

import by.fly.model.Customer;
import by.fly.model.OrderItem;
import by.fly.model.OrderStatus;
import by.fly.model.QCustomer;
import by.fly.repository.CustomerRepository;
import by.fly.repository.OrderItemRepository;
import by.fly.ui.control.OrderItemControl;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

@Component
public class OrdersController extends AbstractController {

    private static final int PAGE_SIZE = 5;

    public Region orderTableRegion;
    public Region createOrderRegion;

    public Label numberLabel;
    public Label orderDateLabel;

    public TableColumn<OrderItem, String> createdAtColumn;
    public TableColumn<OrderItem, String> numberColumn;
    public TableColumn<OrderItem, String> printerTypeColumn;
    public TableColumn<OrderItem, String> orderCodeColumn;
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
    public TextField clientPhoneText;
    public TextField clientNameText;

    private GetOrdersService service = new GetOrdersService();

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CustomerRepository customerRepository;

    public void createOrder(ActionEvent actionEvent) {
        orderItems.getChildren().clear();
        orderItems.getChildren().add(new OrderItemControl());

        numberLabel.setText("Заказ №" + orderItemRepository.count());
        orderDateLabel.setText(DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now()));

        orderTableRegion.toBack();
        createOrderRegion.toFront();
    }

    public void saveOrder(ActionEvent actionEvent) {
        createOrderRegion.toBack();
        orderTableRegion.toFront();

        orderItems.getChildren().forEach(node -> {
            OrderItem orderItem = ((OrderItemControl) node).createOrderItem();
            orderItem.setStatus(OrderStatus.IN_PROGRESS);
            String clientName = clientNameText.getText();
            String clientPhone = clientPhoneText.getText();
            Customer customer = customerRepository.findOne(QCustomer.customer.name.eq(clientName).and(QCustomer.customer.phone.eq(clientPhone)));
            if (customer == null) {
                customer = new Customer(clientName, clientPhone);
                customerRepository.save(customer);
            }
            orderItem.setCustomer(customer);
            orderItemRepository.save(orderItem);
        });

        service.restart();

        refreshPagination();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        initializeColumns();
        bindService();
        refreshPagination();
    }

    private void bindService() {
        progressIndicator.setMaxSize(150, 150);
        progressIndicator.progressProperty().bind(service.progressProperty());
        progressIndicator.visibleProperty().bind(service.runningProperty());
        ordersTable.itemsProperty().bind(service.valueProperty());
        pagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> service.restart());
        service.start();
    }

    private void initializeColumns() {
        numberColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        orderCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrderCode()));
        barCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBarcode()));
        printerTypeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrinterType()));
        printerModelColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrinterModel()));
        workTypeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getWorkType()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().getMessage()));
        priceColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getPrice())));
        clientNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCustomer().getName()));
        clientPhoneColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCustomer().getPhone()));
        createdAtColumn.setCellValueFactory(data -> new SimpleStringProperty(DateTimeFormatter.ISO_TIME.format(data.getValue().getCreatedAt())));
        deadLineColumn.setCellValueFactory(data -> new SimpleStringProperty(DateTimeFormatter.ISO_TIME.format(data.getValue().getDeadLine())));
    }

    public void addOrderItem(ActionEvent actionEvent) {
        orderItems.getChildren().add(new OrderItemControl());
    }

    public void cancelOrder(ActionEvent actionEvent) {
    }

    private class GetOrdersService extends Service<ObservableList<OrderItem>> {
        @Override
        protected Task<ObservableList<OrderItem>> createTask() {
            return new Task<ObservableList<OrderItem>>() {
                @Override
                protected ObservableList<OrderItem> call() throws Exception {
                    Page<OrderItem> orderItems = orderItemRepository.findAll(new PageRequest(pagination.getCurrentPageIndex(), PAGE_SIZE));
                    return FXCollections.observableList(orderItems.getContent());
                }
            };
        }
    }

    private void refreshPagination() {
        int totalCount = (int) orderItemRepository.count();
        float floatCount = Float.valueOf(totalCount) / Float.valueOf(PAGE_SIZE);
        int intCount = totalCount / PAGE_SIZE;

        pagination.setPageCount((floatCount > intCount) ? ++intCount : intCount);
    }
}
