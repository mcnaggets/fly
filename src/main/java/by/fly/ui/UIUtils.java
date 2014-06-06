package by.fly.ui;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class UIUtils {

    public static final int DEFAULT_PAGE_SIZE = 20;

    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);

    private UIUtils() {
        // singleton
    }

    public static int getRowIndex(Pagination pagination, TableColumn.CellDataFeatures c) {
        return getRowIndex(pagination, c, DEFAULT_PAGE_SIZE);
    }

    public static int getRowIndex(Pagination pagination, TableColumn.CellDataFeatures c, int pageSize) {
        return pagination.getCurrentPageIndex() * pageSize + c.getTableView().getItems().indexOf(c.getValue()) + 1;
    }

    public static void refreshPagination(Pagination pagination, int totalCount) {
        refreshPagination(pagination, totalCount, DEFAULT_PAGE_SIZE);
    }

    public static void refreshPagination(Pagination pagination, int totalCount, int pageSize) {
        float floatCount = (float) totalCount / pageSize;
        int intCount = totalCount / DEFAULT_PAGE_SIZE;

        if (intCount == 0) {
            intCount = 1;
        }

        pagination.setPageCount((floatCount > intCount) ? ++intCount : intCount);
    }

    public static void makeDraggable(final Stage stage, final Node byNode) {
        final Delta dragDelta = new Delta();
        byNode.setOnMousePressed(mouseEvent -> {
            // record a delta distance for the drag and drop operation.
            dragDelta.x = stage.getX() - mouseEvent.getScreenX();
            dragDelta.y = stage.getY() - mouseEvent.getScreenY();
            byNode.setCursor(Cursor.MOVE);
        });
        byNode.setOnMouseReleased(mouseEvent -> byNode.setCursor(Cursor.HAND));
        byNode.setOnMouseDragged(mouseEvent -> {
            stage.setX(mouseEvent.getScreenX() + dragDelta.x);
            stage.setY(mouseEvent.getScreenY() + dragDelta.y);
        });
        byNode.setOnMouseEntered(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                byNode.setCursor(Cursor.HAND);
            }
        });
        byNode.setOnMouseExited(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                byNode.setCursor(Cursor.DEFAULT);
            }
        });
    }

    /** records relative x and y co-ordinates. */
    private static class Delta {
        double x, y;
    }

}
