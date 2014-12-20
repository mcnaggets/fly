package by.fly.model.facet;

import by.fly.model.QOrderItem;
import com.mysema.query.types.Path;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderItemFacets {

    public static final Path[] AVAILABLE_PROPERTIES = new Path[]{
            QOrderItem.orderItem.printerModel,
            QOrderItem.orderItem.workTypes,
            QOrderItem.orderItem.masterName,
            QOrderItem.orderItem.itemType
    };

    private Map<Path, List<ValueCount>> data = new HashMap<>();

    private float totalPrice;

    public void addFacets(Path path, List<ValueCount> facets) {
        data.put(path, facets);
    }

    public String getFacetsAsString(Path path, String delimiter) {
        return data.get(path).stream().map(ValueCount::toString).collect(Collectors.joining(delimiter));
    }

    public float getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(float totalPrice) {
        this.totalPrice = totalPrice;
    }
}
