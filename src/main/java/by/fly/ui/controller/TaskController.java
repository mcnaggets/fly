package by.fly.ui.controller;

import by.fly.model.OrderItem;
import by.fly.model.OrderStatus;
import by.fly.model.QOrderItem;
import by.fly.service.OrderService;
import by.fly.ui.UIUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

import static java.time.temporal.ChronoUnit.*;

@Component
public class TaskController extends AbstractController {

    public ListView<StackPane> taskList;
    public ProgressIndicator progressIndicator;

    @Autowired
    private OrderService orderService;

    private final GetOrdersService service = new GetOrdersService();

    private static final Background ORANGE_BACKGROUND = new Background(new BackgroundFill(Color.ORANGE, new CornerRadii(5), null));
    private static final Background GREEN_BACKGROUND = new Background(new BackgroundFill(Color.PALEGREEN, new CornerRadii(5), null));
    private static final Background RED_BACKGROUND = new Background(new BackgroundFill(Color.INDIANRED, new CornerRadii(5), null));

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        progressIndicator.setMaxSize(150, 150);
        progressIndicator.progressProperty().bind(service.progressProperty());
        progressIndicator.visibleProperty().bind(service.runningProperty());
        taskList.itemsProperty().bind(service.valueProperty());

        Timeline timeline = new Timeline(new KeyFrame(Duration.minutes(1), event -> service.restart()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    class GetOrdersService extends Service<ObservableList<StackPane>> {

        @Override
        protected Task<ObservableList<StackPane>> createTask() {
            return new GetOrdersTask();
        }

    }

    class GetOrdersTask extends Task<ObservableList<StackPane>> {

        private LocalDateTime now;
        private LocalDateTime minDeadLine;
        private LocalDateTime maxDeadLine;
        private long wholeIntervalInMinutes;

        @Override
        protected ObservableList<StackPane> call() throws Exception {
            final ObservableList<StackPane> tasks = FXCollections.observableArrayList();
            final List<OrderItem> orderItems = getOrderItems();
            if (orderItems.isEmpty()) return tasks;

            initializeDates(orderItems);

            orderItems.forEach(order -> {
                final long betweenDays = DAYS.between(now, order.getDeadLine());
                final long betweenHours = HOURS.between(now, order.getDeadLine()) % 24;
                final long betweenMinutes = MINUTES.between(now, order.getDeadLine()) % 60;

                int completed = order.getDeadLine().isBefore(now) ? 0 : wholeIntervalInMinutes == 0 ? 100 :
                        (int) (100 * (float) MINUTES.between(minDeadLine, order.getDeadLine()) / wholeIntervalInMinutes);

                GridPane gridPane = createGridPane(completed);
                Label label = createTaskLabel(order, betweenDays, betweenHours, betweenMinutes, gridPane);

                StackPane stackPane = new StackPane(gridPane, label);
                tasks.add(stackPane);
            });

            return tasks;
        }

        private Label createTaskLabel(OrderItem order, long betweenDays, long betweenHours, long betweenMinutes, GridPane gridPane) {
            Label label = new Label();
            label.setWrapText(true);
            if (order.getDeadLine().isBefore(now)) {
                createOverdueRegion(order, betweenDays, betweenHours, betweenMinutes, gridPane, label);
            } else {
                createWorkingRegion(order, betweenDays, betweenHours, betweenMinutes, gridPane, label);
            }
            return label;
        }

        private void initializeDates(List<OrderItem> orderItems) {
            final OrderItem firstItem = orderItems.stream().findFirst().get();
            final OrderItem lastItem = orderItems.stream().reduce((c, p) -> p).get();

            now = LocalDateTime.now();
            minDeadLine = firstItem.getDeadLine().isBefore(now) ? now : firstItem.getDeadLine();
            maxDeadLine = lastItem.getDeadLine().isBefore(now) ? now : lastItem.getDeadLine();
            wholeIntervalInMinutes = MINUTES.between(minDeadLine, maxDeadLine);
        }

        private List<OrderItem> getOrderItems() {
            return orderService.findAll(QOrderItem.orderItem.status.eq(OrderStatus.IN_PROGRESS),
                    new PageRequest(0, 30, Sort.Direction.ASC, QOrderItem.orderItem.deadLine.getMetadata().getName())).getContent();
        }

        private void createWorkingRegion(OrderItem order, long betweenDays, long betweenHours, long betweenMinutes, GridPane gridPane, Label label) {
            Region rect = new Region();
            rect.setBackground(GREEN_BACKGROUND);
            gridPane.add(rect, 0, 0);
            rect = new Pane();
            rect.setBackground(ORANGE_BACKGROUND);
            gridPane.add(rect, 1, 0);

            label.setFont(Font.font("System", 14));
            label.setText(getTaskLabel(order, betweenDays, betweenHours, betweenMinutes, false));
        }

        private void createOverdueRegion(OrderItem order, long betweenDays, long betweenHours, long betweenMinutes, GridPane gridPane, Label label) {
            Region rect = new Region();
            rect.setBackground(RED_BACKGROUND);
            gridPane.add(rect, 1, 0);

            label.setFont(Font.font("System bold", FontWeight.BOLD, 14));
            label.setText(getTaskLabel(order, -betweenDays, -betweenHours, -betweenMinutes, true));
        }

        private GridPane createGridPane(int completed) {
            GridPane gridPane = new GridPane();
            ColumnConstraints constraints = new ColumnConstraints();
            constraints.setPercentWidth(completed);
            gridPane.getColumnConstraints().add(constraints);

            constraints = new ColumnConstraints();
            constraints.setPercentWidth(100 - completed);
            gridPane.getColumnConstraints().add(constraints);

            gridPane.getRowConstraints().add(new RowConstraints(50));
            return gridPane;
        }

        private String getTaskLabel(OrderItem order, long betweenDays, long betweenHours, long betweenMinutes, boolean overdue) {
            return String.format("Заказ №%d %s %s %s %s %s\n%s %s %s %s",
                    order.getOrderNumber(), order.getOrderCode(), order.getBarcode(),
                    order.getPrinterModel(), order.getWorkTypeMessages(", "), UIUtils.TIME_FORMATTER.format(order.getDeadLine()),
                    overdue ? "Задача просрочена на" : "Осталось",
                    UIUtils.daysCountMessage(betweenDays), UIUtils.hoursCountMessage(betweenHours), UIUtils.minutesCountMessage(betweenMinutes));
        }

    }

    @Override
    public void refresh() {
        service.restart();
    }

}
