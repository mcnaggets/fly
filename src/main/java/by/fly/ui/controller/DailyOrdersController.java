package by.fly.ui.controller;

import by.fly.model.statistics.DailyOrders;
import by.fly.model.statistics.QDailyOrders;
import by.fly.service.DailyOrdersService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Pagination;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

import static by.fly.ui.UIUtils.*;

@Component
public class DailyOrdersController extends AbstractController {

    public TableView dailyOrdersTable;

    public ProgressIndicator progressIndicator;
    public Pagination pagination;
    public TableColumn<DailyOrders, String> numberColumn;
    public TableColumn<DailyOrders, String> dateColumn;
    public TableColumn<DailyOrders, String> countColumn;
    public TableColumn<DailyOrders, String> priceColumn;
    public TableColumn<DailyOrders, String> paidColumn;
    public TableColumn<DailyOrders, String> readyColumn;

    private GetDailyOrdersService service = new GetDailyOrdersService();

    @Autowired
    private DailyOrdersService dailyOrdersService;

    private AtomicBoolean doRefreshData = new AtomicBoolean(true);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bindService();
        initializeColumns();
        super.initialize(url, resourceBundle);
    }

    private void initializeColumns() {
        numberColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(getRowIndex(pagination, c))));
        countColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getPaidCount() + c.getValue().getReadyCount())));
        dateColumn.setCellValueFactory(c -> new SimpleStringProperty(DATE_FORMATTER.format(c.getValue().getDate())));
        paidColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getPaidCount())));
        readyColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getReadyCount())));
        priceColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getPrice())));
    }

    private void bindService() {
        progressIndicator.setMaxSize(150, 150);
        progressIndicator.progressProperty().bind(service.progressProperty());
        progressIndicator.visibleProperty().bind(service.runningProperty());
        dailyOrdersTable.itemsProperty().bind(service.valueProperty());
        pagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> service.restart());
        service.setOnSucceeded(e -> updatePagination());
        service.setOnRunning(e -> dailyOrdersTable.setPlaceholder(new Text("Загрузка...")));
        service.start();
    }

    public void refreshData() {
        doRefreshData.set(true);
        service.restart();
    }


    private class GetDailyOrdersService extends Service<ObservableList<DailyOrders>> {
        @Override
        protected Task<ObservableList<DailyOrders>> createTask() {
            return new Task<ObservableList<DailyOrders>>() {
                @Override
                protected ObservableList<DailyOrders> call() throws Exception {
                    if (doRefreshData.compareAndSet(true, false)) {
                        dailyOrdersService.refreshData();
                    }
                    Page<DailyOrders> orderItems = dailyOrdersService.findAll(
                            new PageRequest(pagination.getCurrentPageIndex(), DEFAULT_PAGE_SIZE, new Sort(Sort.Direction.DESC, QDailyOrders.dailyOrders.date.getMetadata().getName()))
                    );
                    return FXCollections.observableList(orderItems.getContent());
                }
            };
        }
    }

    private void updatePagination() {
        int totalCount = (int) dailyOrdersService.count();
        refreshPagination(pagination, totalCount);
    }

}
