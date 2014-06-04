package by.fly.ui;

import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;

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
        float floatCount = Float.valueOf(totalCount) / Float.valueOf(pageSize);
        int intCount = totalCount / DEFAULT_PAGE_SIZE;

        if (intCount == 0) {
            intCount = 1;
        }

        pagination.setPageCount((floatCount > intCount) ? ++intCount : intCount);
    }

}
