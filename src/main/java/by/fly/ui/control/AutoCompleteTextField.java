package by.fly.ui.control;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;

/**
 *
 * @author Narayan G. Maharjan
 * @author Yann D'Isanto
 */
public class AutoCompleteTextField<T> extends Control {

    private static final int DEFAULT_LIMIT = 0;

    private static final int DEFAULT_POPUP_SIZE = 6;

    /**
     * The text property.
     */
    private StringProperty text = new SimpleStringProperty("");

    /**
     * The available items for auto-complete.
     */
    private final ObjectProperty<ObservableList<T>> items = new SimpleObjectProperty<>(FXCollections.<T>observableArrayList());

    /**
     * The maximum number of items the popup can contain. 0 meaning no limit.
     */
    private final IntegerProperty limit = new SimpleIntegerProperty(DEFAULT_LIMIT);

    /**
     * The number of items visible in the popup without scrolling.
     */
    private final IntegerProperty popupSize = new SimpleIntegerProperty(DEFAULT_POPUP_SIZE);

    /**
     * Specifies how the text should be aligned when there is empty space within
     * the TextField.
     */
    private final ObjectProperty<Pos> alignment = new SimpleObjectProperty<>();

    /**
     * The action handler associated with this text field, or null if no action
     * handler is assigned. The action handler is normally called when the user
     * types the ENTER key.
     */
    private final ObjectProperty<EventHandler<ActionEvent>> onAction = new SimpleObjectProperty<>();

    /**
     * The preferred number of text columns. This is used for calculating the
     * TextField's preferred width.
     */
    private final IntegerProperty prefColumnCount = new SimpleIntegerProperty(TextField.DEFAULT_PREF_COLUMN_COUNT);

    /**
     * The TextField's prompt text to display, or null if no prompt text is
     * displayed.
     */
    private final StringProperty promptText = new SimpleStringProperty();

    /**
     * The DataToString used to generate items string representation. By default
     * it uses the toString() method.
     */
    private ObjectProperty<DataToString<T>> dataToString =
            new SimpleObjectProperty<DataToString<T>>(new DefaultDataToString<T>());

    /**
     * Constructor.
     */
    public AutoCompleteTextField() {
        // setup the CSS
        // the -fx-skin attribute in the CSS sets which Skin class is used
        this.getStyleClass().add(this.getClass().getSimpleName().toLowerCase());
    }

    /**
     * Return the path to the CSS file so things are setup right
     */
    @Override
    protected String getUserAgentStylesheet() {
        return this.getClass().getResource("/fxml/" + this.getClass().getSimpleName() + ".css").toString();
    }

    /**
     * Returns the text property.
     *
     * @return the text property.
     */
    public StringProperty textProperty() {
        return text;
    }

    /**
     * Returns the control text value.
     *
     * @return the control text value.
     */
    public String getText() {
        return text.get();
    }

    /**
     * Sets the control text value.
     *
     * @param text the text to set.
     */
    public void setText(String text) {
        this.text.set(text);
    }

    /**
     * The auto-complete items.
     *
     * @see #getItems()
     * @see #setItems(javafx.collections.ObservableList)
     */
    public ObjectProperty<ObservableList<T>> itemsProperty() {
        return items;
    }

    /**
     * Gets the value of the property items.
     */
    public ObservableList<T> getItems() {
        return items.get();
    }

    /**
     * Sets the value of the property items.
     */
    public void setItems(ObservableList<T> items) {
        this.items.set(items);
    }

    /**
     * Returns the limit property.
     *
     * @return the limit property.
     */
    public IntegerProperty limitProperty() {
        return limit;
    }

    /**
     * Returns the maximum number of items the popup can contain. 0 meaning no
     * limit.
     *
     * @return the auto-complete items count limit.
     */
    public int getLimit() {
        return limit.get();
    }

    /**
     * Sets the maximum number of items the popup can contain.
     *
     * @param limit the maximum number of items the popup can contain. 0 meaning
     * no limit.
     */
    public void setLimit(int limit) {
        this.limit.set(limit);
    }

    /**
     * Returns the popupSize property.
     *
     * @return the popupSize property.
     */
    public IntegerProperty popupSize() {
        return popupSize;
    }

    /**
     * The number of number of items visible in the popup without scrolling.
     *
     * @return the popup size as item count.
     */
    public int getPopupSize() {
        return popupSize.get();
    }

    /**
     * The number of number of items visible in the popup without scrolling.
     *
     * @param popupSize the popup size to set.
     */
    public void setPopupSize(int popupSize) {
        this.popupSize.set(popupSize);
    }

    /**
     * Returns the dataToString property.
     *
     * @return the dataToString property.
     */
    public ObjectProperty<DataToString<T>> dataToStringProperty() {
        return dataToString;
    }

    /**
     * Returns the DataToString used to retreive items string representation.
     *
     * @return a DataToString instance.
     */
    public DataToString<T> getDataToString() {
        return dataToString.get();
    }

    /**
     * Sets the DataToString used to retreive items string representation.
     *
     * @param dataToString the DataToString instance to use for retreiving items
     * string representation.
     */
    public void setDataToString(DataToString<T> dataToString) {
        this.dataToString.set(dataToString);
    }

    /**
     * The TextField's prompt text to display, or null if no prompt text is
     * displayed.
     *
     * @see #getPromptText()
     * @see #setPromptText(java.lang.String)
     */
    public StringProperty promptTextProperty() {
        return promptText;
    }

    /**
     * Gets the value of the property promptText.
     */
    public String getPromptText() {
        return promptText.get();
    }

    /**
     * Sets the value of the property promptText.
     */
    public void setPromptText(String promptText) {
        this.promptText.set(promptText);
    }

    /**
     * Specifies how the text should be aligned when there is empty space within
     * the TextField.
     *
     * @see #getAlignment()
     * @see #setAlignment(javafx.geometry.Pos)
     */
    public ObjectProperty<Pos> alignmentProperty() {
        return alignment;
    }

    /**
     * Gets the value of the property alignment.
     */
    public Pos getAlignment() {
        return alignment.get();
    }

    /**
     * Sets the value of the property alignment.
     */
    public void setAlignment(Pos alignment) {
        this.alignment.set(alignment);
    }

    /**
     * The action handler associated with this text field, or null if no action
     * handler is assigned. The action handler is normally called when the user
     * types the ENTER key.
     *
     * @see #getOnAction()
     * @see #setOnAction(javafx.event.EventHandler)
     */
    public ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
        return onAction;
    }

    /**
     * Gets the value of the property onAction.
     */
    public EventHandler<ActionEvent> getOnAction() {
        return onAction.get();
    }

    /**
     * Sets the value of the property onAction.
     */
    public void setOnAction(EventHandler<ActionEvent> onAction) {
        this.onAction.set(onAction);
    }

    /**
     * The preferred number of text columns. This is used for calculating the
     * TextField's preferred width.
     *
     * @see #getPrefColumnCount()
     * @see #setPrefColumnCount(int)
     */
    public IntegerProperty prefColumnCountProperty() {
        return prefColumnCount;
    }

    /**
     * Gets the value of the property prefColumnCount.
     */
    public int getPrefColumnCount() {
        return prefColumnCount.get();
    }

    /**
     * Sets the value of the property prefColumnCount.
     */
    public void setPrefColumnCount(int prefColumnCount) {
        this.prefColumnCount.set(prefColumnCount);
    }

    /**
     * A default implementation of
     * <code>DataToString</code> interface relying on objects
     * <code>toString()</code> method.
     *
     * @param <T> the data type
     */
    public static final class DefaultDataToString<T> implements DataToString<T> {

        @Override
        public String toString(T item) {
            return item == null ? null : item.toString();
        }
    }
}