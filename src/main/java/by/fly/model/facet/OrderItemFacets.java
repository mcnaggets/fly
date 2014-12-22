package by.fly.model.facet;

import by.fly.model.QOrderItem;
import com.mysema.query.types.Path;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OrderItemFacets {

    public static final Path[] AVAILABLE_PROPERTIES = new Path[]{
            QOrderItem.orderItem.printerModel,
            QOrderItem.orderItem.workTypes,
            QOrderItem.orderItem.masterName,
            QOrderItem.orderItem.itemType
    };

    public static final Function<ValueCount, String> DEFAULT_DATA_TRANSFORMER = ValueCount::toString;
    public static final String DEFAULT_DELIMITER = "\n";

    private Map<Path, List<ValueCount>> data = new HashMap<>();

    private float totalPrice;

    public void addFacets(Path path, List<ValueCount> facets) {
        data.put(path, facets);
    }

    public String getFacetsAsString(Path path) {
        return getFacetsAsString(path, DEFAULT_DELIMITER);
    }

    public String getFacetsAsString(Path path, String delimiter) {
        return getFacetsAsString(path, DEFAULT_DATA_TRANSFORMER, delimiter);
    }

    public String getFacetsAsString(Path path, Function<ValueCount, String> dataTransformer) {
        return getFacetsAsString(path, dataTransformer, DEFAULT_DELIMITER);
    }

    public String getFacetsAsString(Path path, Function<ValueCount, String> dataTransformer, String delimiter) {
        return data.get(path).stream().map(dataTransformer).collect(Collectors.joining(delimiter));
    }

    public float getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(float totalPrice) {
        this.totalPrice = totalPrice;
    }
}
