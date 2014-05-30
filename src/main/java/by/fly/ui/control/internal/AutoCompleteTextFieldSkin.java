package by.fly.ui.control.internal;

import by.fly.ui.control.AutoCompleteTextField;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Callback;

/**
 * AutoCompleteTextField control skin.
 *
 * @author Narayan G. Maharjan
 * @author Yann D'Isanto
 */
public class AutoCompleteTextFieldSkin<T> extends BehaviorSkinBase<AutoCompleteTextField<T>, AutoCompleteTextFieldBehavior<T>> {

    /**
     * ListView for showing the matched words.
     */
    private final ListView<T> listView = new ListView<>();

    /**
     * Entry TextField.
     */
    private final TextField textField = new TextField();

    /**
     * Popup to display matched word ListView in.
     */
    private Popup popup = new Popup();

    /**
     * Constructor.
     *
     * @param control the skinned control.
     */
    public AutoCompleteTextFieldSkin(final AutoCompleteTextField control) {
        super(control, new AutoCompleteTextFieldBehavior(control));
        initialize();
    }

    /**
     * Initializes this skin.
     */
    private void initialize() {
        final AutoCompleteTextField control = getSkinnable();
        control.textProperty().bindBidirectional(textField.textProperty());
        control.setAlignment(textField.getAlignment());
        textField.promptTextProperty().bind(control.promptTextProperty());
        textField.alignmentProperty().bind(control.alignmentProperty());
        textField.onActionProperty().bind(control.onActionProperty());
        textField.prefColumnCountProperty().bind(control.prefColumnCountProperty());

        getBehavior().initialize(this);

        //This cell factory helps to know which cell has been selected so that
        //when ever any cell is selected the textbox rawText must be changed
        listView.setCellFactory(new Callback<ListView<T>, ListCell<T>>() {
            @Override
            public ListCell<T> call(ListView<T> p) {
                //A simple ListCell containing only Label
                final ListCell<T> cell = new ListCell<T>() {
                    @Override
                    public void updateItem(T item,
                            boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(control.getDataToString().toString(item));
                        }
                    }
                };
                getBehavior().registerCellFocusInvalidationListener(cell);
                return cell;
            }
        });
        textField.focusedProperty().addListener((observable, oldValue, newValue) -> textField.end());

        //popup
        popup = new Popup();
        popup.setAutoHide(true);
        popup.getContent().add(listView);

        //Adding textbox in this control Children
        getChildren().addAll(textField);
    }

    /**
     * A Popup containing Listview is trigged from this function This function
     * automatically resize it's height and width according to the width of
     * textbox and item's cell height
     */
    public void showPopup() {
        int itemsCount = listView.getItems().size();
        if (itemsCount == 0) {
            return;
        }
        listView.setPrefWidth(textField.getWidth());
        final int cellHeight = 24; // TODO: retreive cell height
        int popupSize = getSkinnable().getPopupSize();
        int itemsToShow = itemsCount > popupSize ? popupSize : itemsCount;
        int prefHeight = itemsToShow * cellHeight;
        if (prefHeight > 0) {
            int listPadding = 2; // TODO: retreive value
            prefHeight += listPadding;
        }
        listView.setPrefHeight(prefHeight);

        // Calculating the x and y popup position so it is displayed just below the textfield.
        Scene scene = getSkinnable().getScene();
        Window window = scene.getWindow();
        Point2D fieldPosition = textField.localToScene(0, 0);
        popup.show(
                window,
                window.getX() + fieldPosition.getX() + scene.getX(),
                window.getY() + fieldPosition.getY() + scene.getY() + textField.getHeight());

        listView.getSelectionModel().clearSelection();
        listView.getFocusModel().focus(-1);
    }

    /**
     * This function hides the popup containing listview
     */
    public void hidePopup() {
        popup.hide();
    }

    /**
     * Returns true if the auto-complete popup is displayed.
     *
     * @return true if the auto-complete popup is displayed.
     */
    public boolean isPopupShowing() {
        return popup.isShowing();
    }

    /**
     * Returns the TextField used by this skin.
     *
     * @return a TextField instance.
     */
    public TextField getTextField() {
        return textField;
    }

    /**
     * Returns the ListView used by this skin.
     *
     * @return a ListView instance.
     */
    public ListView<T> getListView() {
        return listView;
    }
}