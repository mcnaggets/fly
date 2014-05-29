package by.fly.ui.controller;

import by.fly.model.OrderItem;
import by.fly.model.OrderStatus;
import by.fly.repository.OrderItemRepository;
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
public class TaskViewController extends AbstractController {

    public ListView<StackPane> taskList;
    public ProgressIndicator progressIndicator;

    @Autowired
    OrderItemRepository orderItemRepository;

    private GetOrdersService service = new GetOrdersService();

    static final Background orangeBackground = new Background(new BackgroundFill(Color.ORANGE, null, null));
    static final Background greenBackground = new Background(new BackgroundFill(Color.LIGHTGREEN, null, null));
    static final Background redBackground = new Background(new BackgroundFill(Color.ORANGERED, null, null));

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        progressIndicator.setMaxSize(150, 150);
        progressIndicator.progressProperty().bind(service.progressProperty());
        progressIndicator.visibleProperty().bind(service.runningProperty());
        taskList.itemsProperty().bind(service.valueProperty());

        service.start();

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(10), event -> service.restart()));
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

            ObservableList<StackPane> tasks = FXCollections.observableArrayList();

            final List<OrderItem> orderItems = orderItemRepository.findByStatus(OrderStatus.IN_PROGRESS,
                    new PageRequest(0, 30, Sort.Direction.ASC, "deadLine"));

            if (orderItems.isEmpty()) {
                return tasks;
            }

            final LocalDateTime minDeadLine = orderItems.get(0).getDeadLine();
            final LocalDateTime maxDeadLine = orderItems.get(orderItems.size() - 1).getDeadLine();
            final LocalDateTime now = LocalDateTime.now();

            final long wholeIntervalInMinutes = MINUTES.between(minDeadLine, maxDeadLine);

            orderItems.forEach(order -> {
                int completed = (int) (100 * MINUTES.between(minDeadLine, order.getDeadLine()) / wholeIntervalInMinutes);

                GridPane gridPane = new GridPane();
                ColumnConstraints constraints = new ColumnConstraints();
                constraints.setPercentWidth(completed);
                gridPane.getColumnConstraints().add(constraints);

                constraints = new ColumnConstraints();
                constraints.setPercentWidth(100 - completed);
                gridPane.getColumnConstraints().add(constraints);

                gridPane.getRowConstraints().add(new RowConstraints(40));

                final long between = HOURS.between(now, order.getDeadLine());

                Label label = new Label();
                if (between < 0) {
                    Region rect = new Region();
                    rect.setBackground(redBackground);
                    gridPane.add(rect, 1, 0);

                    label.setText("Задача просрочена на " + -between + " часа");
                } else {
                    Region rect = new Region();
                    rect.setBackground(greenBackground);
                    gridPane.add(rect, 0, 0);
                    rect = new Pane();
                    rect.setBackground(orangeBackground);
                    gridPane.add(rect, 1, 0);

                    label.setText("Осталось " + between + " часа");

                }

                StackPane stackPane = new StackPane(gridPane, label);

                tasks.add(stackPane);
            });

            return tasks;
        }
    }

    public void refreshTasks(ActionEvent actionEvent) {
        service.restart();
    }

}
