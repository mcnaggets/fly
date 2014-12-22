package by.fly.ui;

import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.stream.IntStream;

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

    public static String daysCountMessage(long days) {
        return days > 0 ? days % 10 > 0 ? days < 11 | days > 14 ? days % 10 > 1 ? days % 10 > 4 ? days + " дней" : days + " дня" : days + " день" : days + " дней" : days + " дней" : "";
    }

    public static String hoursCountMessage(long hours) {
        return hours > 0 ? hours % 10 > 0 ? hours < 11 | hours > 14 ? hours % 10 > 1 ? hours % 10 > 4 ? hours + " часов" : hours + " часа" : hours + " час" : hours + " часов" : hours + " часов" : "";
    }

    public static String minutesCountMessage(long minutes) {
        return minutes > 0 ? minutes % 10 > 0 ? minutes < 11 | minutes > 14 ? minutes % 10 > 1 ? minutes % 10 > 4 ? minutes + " минут" : minutes + " минуты" : minutes + " минута" : minutes + " минут" : minutes + " минут" : "";
    }

    public static void main(String[] args) {
        IntStream.range(1, 102).forEach(i -> System.out.println(i + " " + daysCountMessage(i) + " " + hoursCountMessage(i) + " " + minutesCountMessage(i)));
    }

}
