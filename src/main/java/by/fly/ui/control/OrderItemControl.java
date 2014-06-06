package by.fly.ui.control;

import by.fly.model.OrderItem;
import by.fly.model.PrinterType;
import by.fly.model.WorkType;
import by.fly.service.OrderService;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import jfxtras.scene.control.LocalTimeTextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

import static by.fly.ui.UIUtils.TIME_FORMATTER;
import static jdk.nashorn.internal.runtime.JSType.isNumber;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class OrderItemControl extends FlowPane {

    @Autowired
    private OrderService orderService;

    private TextField barcodeText;
    private ComboBox<PrinterType> printerTypeCombo;
    private TextField printerModelText;
    private LocalTimeTextField deadLineTimePicker;
    private DatePicker deadLineDatePicker;
    private ToggleGroup workTypeGroup;
    private TextArea descriptionArea;
    private RestrictiveTextField priceText;

    private final ObjectProperty<EventHandler<ActionEvent>> onPriceChanged = new SimpleObjectProperty<>();
    private final ObjectProperty<EventHandler<ActionEvent>> onBarcodeChanged = new SimpleObjectProperty<>();

    public void initialize() {
        createChildren();
        setBorder(new Border(
                new BorderStroke(Color.GRAY, BorderStrokeStyle.DOTTED, new CornerRadii(5), BorderStroke.DEFAULT_WIDTHS)
        ));
    }

    private void createChildren() {
        GridPane grid = createGridPane();

        createBarcodeText();
        createColumnConstraints(grid);
        createPrinterTypeCombo();
        createPrinterModelText();
        createDeadLineControls();
        createDescriptionArea();
        createPriceText();

        workTypeGroup = new ToggleGroup();
        populateGrid(grid);
        workTypeGroup.selectToggle(workTypeGroup.getToggles().iterator().next());

        getChildren().add(grid);
    }

    private void createBarcodeText() {
        barcodeText = new TextField();
        barcodeText.textProperty().addListener(e -> barcodeChanged());
    }

    private void barcodeChanged() {
        String barcode = getBarcode();
        OrderItem item = orderService.findLastItemByBarcode(barcode);
        if (item != null) {
            printerModelText.setText(item.getPrinterModel());
            printerTypeCombo.setValue(item.getPrinterType());
            onBarcodeChanged.get().handle(new ActionEvent(barcode, this));
        }
    }

    private void populateGrid(GridPane grid) {
        TextField currentTime = new TextField(TIME_FORMATTER.format(LocalTime.now()));
        currentTime.setEditable(false);

        grid.add(new Label("Штрихкод:"), 0, 0);
        grid.add(barcodeText, 1, 0);
        grid.add(new Label("Тип:"), 2, 0);
        grid.add(printerTypeCombo, 3, 0);
        grid.add(new Label("Модель:"), 6, 0);
        grid.add(printerModelText, 7, 0);

        grid.add(new Label("Примечание:"), 0, 1);
        grid.add(descriptionArea, 1, 1, 5, 1);

        grid.add(new Label("Текущее время:"), 0, 2);
        grid.add(currentTime, 1, 2);
        grid.add(new Label("Время исполнения:"), 2, 2);
        grid.add(deadLineDatePicker, 3, 2);
        grid.add(deadLineTimePicker, 5, 2);

        grid.add(new Label("Стоимость:"), 6, 2);
        grid.add(priceText, 7, 2);

        grid.add(new Label("Вид работ:"), 6, 1);
        grid.add(new VBox(5, Arrays.asList(WorkType.values()).stream().map(workType -> {
            RadioButton radioButton = new RadioButton(workType.getMessage());
            radioButton.setToggleGroup(workTypeGroup);
            radioButton.setUserData(workType);
            return radioButton;
        }).toArray(Node[]::new)), 7, 1);
    }

    private void createDescriptionArea() {
        descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(3);
    }

    private void createDeadLineControls() {
        deadLineTimePicker = new LocalTimeTextField(LocalTime.now());
        deadLineDatePicker = new DatePicker(LocalDate.now());
        deadLineDatePicker.setPrefWidth(100);
    }

    private void createPrinterModelText() {
        printerModelText = new TextField();
    }

    private GridPane createGridPane() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);
        return grid;
    }

    private void createColumnConstraints(GridPane grid) {
        ColumnConstraints constraints = new ColumnConstraints();
        constraints.setHalignment(HPos.RIGHT);
        grid.getColumnConstraints().add(constraints);
        grid.getColumnConstraints().add(new ColumnConstraints(70));
        grid.getColumnConstraints().add(constraints);
        grid.getColumnConstraints().add(constraints);
        grid.getColumnConstraints().add(constraints);
    }

    private void createPrinterTypeCombo() {
        printerTypeCombo = new ComboBox<>(FXCollections.<PrinterType>observableArrayList(PrinterType.LASER, PrinterType.JET));
        printerTypeCombo.setValue(PrinterType.LASER);
        printerTypeCombo.setConverter(new StringConverter<PrinterType>() {
            @Override
            public String toString(PrinterType object) {
                return object.getMessage();
            }

            @Override
            public PrinterType fromString(String string) {
                return PrinterType.fromMessage(string);
            }
        });
    }

    private void createPriceText() {
        priceText = new RestrictiveTextField();
        priceText.setText("0");
        priceText.setRestrict("[0-9]");
        priceText.setMaxLength(10);
        priceText.textProperty().addListener(e -> priceChanged());
    }

    private void priceChanged() {
        if (isNumber(priceText.getText())) {
            onPriceChanged.get().handle(new ActionEvent(getPrice(), this));
        }
    }

    public float getPrice() {
        return Float.valueOf(priceText.getText());
    }

    public String getBarcode() {
        return barcodeText.getText();
    }

    public void setOnPriceChanged(EventHandler<ActionEvent> handler) {
        this.onPriceChanged.set(handler);
    }

    public void setOnBarcodeChanged(EventHandler<ActionEvent> handler) {
        this.onBarcodeChanged.set(handler);
    }

    public OrderItem createOrderItem() {
        OrderItem orderItem = new OrderItem(LocalDateTime.of(deadLineDatePicker.getValue(), deadLineTimePicker.getLocalTime()));
        orderItem.setBarcode(barcodeText.getText());
        orderItem.setPrinterModel(printerModelText.getText());
        orderItem.setPrinterType(printerTypeCombo.getValue());
        orderItem.setWorkType((WorkType) workTypeGroup.getSelectedToggle().getUserData());
        orderItem.setDescription(descriptionArea.getText());
        orderItem.setPrice(getPrice());
        return orderItem;
    }

}
