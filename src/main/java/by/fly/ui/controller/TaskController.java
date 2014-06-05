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
import javafx.event.ActionEvent;
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

import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;

@Component
public class TaskController extends AbstractController {

    public ListView<StackPane> taskList;
    public ProgressIndicator progressIndicator;

    @Autowired
    private OrderService orderService;

    private GetOrdersService service = new GetOrdersService();

    static final Background orangeBackground = new Background(new BackgroundFill(Color.ORANGE, new CornerRadii(5), null));
    static final Background greenBackground = new Background(new BackgroundFill(Color.PALEGREEN, new CornerRadii(5), null));
    static final Background redBackground = new Background(new BackgroundFill(Color.CRIMSON, new CornerRadii(5), null));

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        progressIndicator.setMaxSize(150, 150);
        progressIndicator.progressProperty().bind(service.progressProperty());
        progressIndicator.visibleProperty().bind(service.runningProperty());
        taskList.itemsProperty().bind(service.valueProperty());

        service.start();

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
        @Override
        protected ObservableList<StackPane> call() throws Exception {

            final ObservableList<StackPane> tasks = FXCollections.observableArrayList();

            final List<OrderItem> orderItems = orderService.findAll(QOrderItem.orderItem.status.eq(OrderStatus.IN_PROGRESS),
                    new PageRequest(0, 30, Sort.Direction.ASC, QOrderItem.orderItem.deadLine.getMetadata().getName())).getContent();

            if (orderItems.isEmpty()) {
                return tasks;
            }

            final OrderItem firstItem = orderItems.stream().findFirst().get();
            final OrderItem lastItem = orderItems.stream().reduce((c, p) -> p).get();

            final LocalDateTime now = LocalDateTime.now();
            final LocalDateTime minDeadLine = firstItem.getDeadLine().isBefore(now) ? now : firstItem.getDeadLine();
            final LocalDateTime maxDeadLine = lastItem.getDeadLine().isBefore(now) ? now : lastItem.getDeadLine();

            final long wholeIntervalInMinutes = MINUTES.between(minDeadLine, maxDeadLine);

            orderItems.forEach(order -> {
                final long betweenHours = HOURS.between(now, order.getDeadLine());
                final long betweenMinutes = MINUTES.between(now, order.getDeadLine()) % 60;

                int completed = isOverdue(betweenHours, betweenMinutes) ? 0 : wholeIntervalInMinutes == 0 ? 100 :
                        (int) (100 * MINUTES.between(minDeadLine, order.getDeadLine()) / wholeIntervalInMinutes);

                GridPane gridPane = new GridPane();
                ColumnConstraints constraints = new ColumnConstraints();
                constraints.setPercentWidth(completed);
                gridPane.getColumnConstraints().add(constraints);

                constraints = new ColumnConstraints();
                constraints.setPercentWidth(100 - completed);
                gridPane.getColumnConstraints().add(constraints);

                gridPane.getRowConstraints().add(new RowConstraints(50));

                Label label = new Label();
                label.setWrapText(true);
                if (isOverdue(betweenHours, betweenMinutes)) {
                    Region rect = new Region();
                    rect.setBackground(redBackground);
                    gridPane.add(rect, 1, 0);

                    label.setFont(Font.font("System bold", FontWeight.BOLD, 14));
                    label.setText(getTaskLabel(order, -betweenHours, -betweenMinutes, true));
                } else {
                    Region rect = new Region();
                    rect.setBackground(greenBackground);
                    gridPane.add(rect, 0, 0);
                    rect = new Pane();
                    rect.setBackground(orangeBackground);
                    gridPane.add(rect, 1, 0);

                    label.setFont(Font.font("System bold", 14));
                    label.setText(getTaskLabel(order, betweenHours, betweenMinutes, false));

                }

                StackPane stackPane = new StackPane(gridPane, label);

                tasks.add(stackPane);
            });

            return tasks;
        }

        private boolean isOverdue(long betweenHours, long betweenMinutes) {
            return betweenHours < 0 || betweenMinutes < 0;
        }

        private String getTaskLabel(OrderItem order, long betweenHours, long betweenMinutes, boolean overdue) {
            String prefix;
            if (overdue) {
                prefix = "Задача просрочена на";
            } else {
                prefix = "Осталось";
            }
            return String.format("Заказ №%d %s %s %s %s %s\n%s %d часа %d минут",
                    order.getOrderNumber(), order.getOrderCode(), order.getBarcode(),
                    order.getPrinterModel(), order.getWorkType().getMessage(), UIUtils.TIME_FORMATTER.format(order.getDeadLine()),
                    prefix, betweenHours, betweenMinutes);
        }
    }

    public void refreshTasks(ActionEvent actionEvent) {
        service.restart();
    }

}
