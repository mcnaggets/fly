package by.fly.ui.control.internal;

import by.fly.ui.control.AutoCompleteTextField;
import by.fly.ui.control.DataToString;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AutoCompleteTextField control behavior.
 *
 * @author Narayan G. Maharjan
 * @author Yann D'Isanto
 */
public final class AutoCompleteTextFieldBehavior<T> extends BehaviorBase<AutoCompleteTextField<T>> {

    private static final Logger LOG = Logger.getLogger(AutoCompleteTextField.class.getName());

    /**
     * Displayed auto-complete items.
     */
    private final ObservableList<T> autoCompleteList = FXCollections.observableArrayList();

    /**
     * Comparator used to sort the displayed items.
     */
    private DataToStringComparator listComparator;

    /**
     * Updating field flag to avoid loop on text change events.
     */
    private boolean updatingField = false;

    /**
     * Control skin.
     */
    private AutoCompleteTextFieldSkin<T> skin;

    /**
     * Temporary text used to display focused item in textfield.
     */
    private String temporaryTxt = "";

    /**
     * Control data list change listener.
     */
    private ListChangeListener<T> dataChangeListener = new ListChangeListener<T>() {
        @Override
        public void onChanged(Change<? extends T> c) {
            while (c.next()) {
                autoCompleteList.removeAll(c.getRemoved());
                extractMatchingItems(c.getAddedSubList(), skin.getTextField().getText().trim().toLowerCase());
            }
        }
    };

    /**
     * Control text change listener.
     */
    private ChangeListener<String> textChangeListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            if (updatingField) {
                return;
            }
            oldValue = oldValue != null ? oldValue : "";
            String refText = newValue != null ? newValue.trim().toLowerCase() : "";
            if (newValue != null && oldValue.length() > 0 && newValue.startsWith(oldValue)) {
                // New value is more accurate than the previous one => narrow down the current auto-complete list.
                List<T> toRemove = new ArrayList<>();
                DataToString<T> dataToString = getControl().getDataToString();
                for (T item : autoCompleteList) {
                    String str = dataToString.toString(item).toLowerCase();
                    if (!str.startsWith(refText)) {
                        toRemove.add(item);
                    }
                }
                autoCompleteList.removeAll(toRemove);
            } else {
                autoCompleteList.clear();
                if (refText.length() > 0) {
                    extractMatchingItems(getControl().getItems(), refText);
                }
            }
            if (autoCompleteList.size() > 0) {
                temporaryTxt = skin.getTextField().getText();
                skin.showPopup();
            } else {
                skin.hidePopup();
            }
        }
    };

    /**
     * Mouse events handler.
     */
    private final EventHandler<? super MouseEvent> mouseEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if (event.getEventType() == MouseEvent.MOUSE_RELEASED && event.getSource() == skin.getListView()) {
                selectAutoCompleteItem();
            }
        }
    };

    /**
     * Key events handler.
     */
    private final EventHandler<? super KeyEvent> keyEventHandler = new EventHandler<KeyEvent>() {
        @Override
        public void handle(KeyEvent event) {
            if (event.getEventType() == KeyEvent.KEY_PRESSED && event.getSource() == skin.getTextField()) {
                KeyEvent t = (KeyEvent) event;
                if (t.getCode() == KeyCode.DOWN) {
                    if (!skin.isPopupShowing()) {
                        String refText = skin.getTextField().getText().trim().toLowerCase();
                        if (refText.isEmpty() && autoCompleteList.isEmpty()) {
                            List<T> toAdd = getControl().getItems();
                            int limit = getControl().getLimit();
                            if (limit > 0) {
                                toAdd = toAdd.subList(0, limit);
                            }
                            autoCompleteList.addAll(toAdd);
                            FXCollections.sort(autoCompleteList, listComparator);
                        }
                        skin.showPopup();
                    } else {
                        skin.getListView().requestFocus();
                        skin.getListView().getSelectionModel().select(0);
                    }
                }
            } else if (event.getEventType() == KeyEvent.KEY_RELEASED && event.getSource() == skin.getListView()) {
                KeyEvent t = (KeyEvent) event;
                switch (t.getCode()) {
                    case ENTER:
                        selectAutoCompleteItem();
                        break;
                    case UP:
                        if (skin.getListView().getSelectionModel().getSelectedIndex() == 0) {
                            skin.getTextField().requestFocus();
                        }
                        break;
                    case ESCAPE:
                        skin.getTextField().replaceText(skin.getTextField().getSelection(), "");
                        skin.getTextField().requestFocus();
                        skin.hidePopup();
                        break;
                }
            }
        }
    };

    /**
     * Constructor.
     *
     * @param control
     */
    public AutoCompleteTextFieldBehavior(AutoCompleteTextField control) {
        super(control, null);
    }

    /**
     * Initializes using the specified skin.
     *
     * @param skin the control skin.
     */
    public void initialize(AutoCompleteTextFieldSkin<T> skin) {
        this.skin = skin;
        AutoCompleteTextField control = getControl();
        listComparator = new DataToStringComparator<>(control.getDataToString());
        control.getItems().addListener(dataChangeListener);
        control.itemsProperty().addListener(new ChangeListener<ObservableList<T>>() {
            @Override
            public void changed(ObservableValue<? extends ObservableList<T>> observable, ObservableList<T> oldValue, ObservableList<T> newValue) {
                if (oldValue != null) {
                    oldValue.removeListener(dataChangeListener);
                }
                if (newValue != null) {
                    newValue.addListener(dataChangeListener);
                }
            }
        });
        control.dataToStringProperty().addListener((observable, oldValue, newValue) -> {
            listComparator = new DataToStringComparator<>((DataToString<Object>) newValue);
            FXCollections.sort(autoCompleteList, listComparator);
        });
        skin.getListView().setOnMouseReleased(mouseEventHandler);
        skin.getListView().setOnKeyReleased(keyEventHandler);
        skin.getListView().setItems(autoCompleteList);
        skin.getTextField().setOnKeyPressed(keyEventHandler);
        skin.getTextField().textProperty().addListener(textChangeListener);
    }

    /**
     * Extracts the items matching the specifed text from the specified items
     * list. Matching items are then added to the auto-complete list.
     *
     * @param items the list to extract items from.
     * @param text  the text items string representation must start with.
     */
    private void extractMatchingItems(List<? extends T> items, String text) {
        int itemsCount = autoCompleteList.size();
        int limit = getControl().getLimit();
        if (text.isEmpty()) {
            List<T> toAdd = getControl().getItems();
            if (limit > 0) {
                toAdd = toAdd.subList(0, limit - itemsCount);
            }
            autoCompleteList.addAll(toAdd);
        } else {
            DataToString<T> dataToString = getControl().getDataToString();
            for (T item : items) {
                if (getControl().getLimit() > 0 && itemsCount == getControl().getLimit()) {
                    break;
                }
                String str = dataToString.toString(item).toLowerCase();
                if (str.startsWith(text)) {
                    autoCompleteList.add(item);
                    itemsCount++;
                }
            }
        }
        FXCollections.sort(autoCompleteList, listComparator);
    }

    /**
     * Populates the textfield according to the current selected item.
     */
    private void selectAutoCompleteItem() {
        T item = skin.getListView().getSelectionModel().getSelectedItem();
        if (item != null) {
            skin.getTextField().setText(getControl().getDataToString().toString(item));
            skin.getTextField().requestFocus();
            skin.getTextField().requestLayout();
            skin.getTextField().end();
            temporaryTxt = "";
            skin.hidePopup();
        }
    }

    /**
     * Adds focus behavior on the specified cell.
     *
     * @param cell auto-complete list view cell.
     */
    public void registerCellFocusInvalidationListener(final ListCell<T> cell) {
        InvalidationListener listener = ove -> {
            if (cell.getItem() != null && cell.isFocused()) {
                //here we are using 'temporaryTxt' as temporary saving text
                //If temporaryTxt length is 0 then assign with current rawText()

                //first check ...(either texmporaryTxt is empty char or not)
                if (temporaryTxt.length() <= 0) {
                    //second check...
                    if (autoCompleteList.size() != getControl().getItems().size()) {
                        temporaryTxt = skin.getTextField().getText();
                    }
                }

                String prev = temporaryTxt;
                updatingField = true;
                String string = getControl().getDataToString().toString(cell.getItem());
                skin.getTextField().textProperty().setValue(string);
                updatingField = false;
                skin.getTextField().selectRange(prev.length(), string.length());
                LOG.log(Level.FINE, "{0}={1}::{2}",
                        new Object[]{
                                temporaryTxt.length(),
                                skin.getTextField().getText().length(),
                                string.length()
                        }
                );
            }
        };
        cell.focusedProperty().addListener(listener);
    }

    /**
     * A comparator based on object string representation. The string
     * representation is retreived using a DataToString instance.
     *
     * @param <T> the data type.
     */
    private final class DataToStringComparator<T> implements Comparator<T> {

        private final DataToString<T> dataToString;

        private final boolean ignoreCase;

        /**
         * Constructor.
         *
         * @param dataToString a DataToString instance.
         */
        public DataToStringComparator(DataToString<T> dataToString) {
            this(dataToString, true);
        }

        /**
         * Constructor.
         *
         * @param dataToString a DataToString instance.
         * @param ignoreCase   ignore case flag.
         */
        public DataToStringComparator(DataToString<T> dataToString, boolean ignoreCase) {
            this.dataToString = dataToString;
            this.ignoreCase = ignoreCase;
        }

        @Override
        public int compare(T o1, T o2) {
            return ignoreCase
                    ? dataToString.toString(o1).compareToIgnoreCase(dataToString.toString(o2))
                    : dataToString.toString(o1).compareTo(dataToString.toString(o2));
        }
    }
}