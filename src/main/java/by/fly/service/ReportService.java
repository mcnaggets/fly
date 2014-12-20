package by.fly.service;

import by.fly.model.OrderItem;
import by.fly.model.QOrderItem;
import by.fly.model.facet.OrderItemFacets;
import by.fly.model.facet.ValueCount;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.CollectionPathBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class ReportService {

    public static final String COUNT_FIELD_NAME = "count";

    @Autowired
    private MongoOperations mongoOperations;

    public OrderItemFacets generateFacets() {
        OrderItemFacets facets = new OrderItemFacets();
        for (Path path : OrderItemFacets.AVAILABLE_PROPERTIES) {
            facets.addFacets(path, getFacets(path));
        }
        return facets;
    }

    public long getTotalPrice() {
        final String sumField = "sum";
        final AggregationOutput output = mongoOperations.execute(call -> call.getCollection(QOrderItem.orderItem.getMetadata().getName()).aggregate(
                Stream.of(
                        new BasicDBObject("$group",
                                BasicDBObjectBuilder.start()
                                        .add("_id", "null")
                                        .add(sumField, new BasicDBObject("$sum", "$" + QOrderItem.orderItem.price.getMetadata().getName())).get())
                ).collect(Collectors.toList())
        ));
        final Number sum = (Number) output.results().iterator().next().get(sumField);
        return sum.longValue();
    }

    private List<ValueCount> getFacets(Path path) {
        final String tagName = path.getMetadata().getName();
        TypedAggregation<OrderItem> aggregation = new TypedAggregation<>(OrderItem.class, getAggregationOperations(path, tagName));
        AggregationResults<DBObject> results = mongoOperations.aggregate(aggregation, DBObject.class);
        return results.getMappedResults().stream().map(obj -> new ValueCount((String) obj.get(tagName),
                (Integer) obj.get(COUNT_FIELD_NAME))).collect(Collectors.toList());
    }

    private List<AggregationOperation> getAggregationOperations(Path path, String tagName) {
        final List<AggregationOperation> aggregationOperations = new LinkedList<>();
        aggregationOperations.add(project(tagName));
        if (path instanceof CollectionPathBase) {
            aggregationOperations.add(unwind(tagName));
        }
        aggregationOperations.add(group(tagName).count().as(COUNT_FIELD_NAME));
        aggregationOperations.add(project(COUNT_FIELD_NAME).and(tagName).previousOperation());
        aggregationOperations.add(sort(DESC, COUNT_FIELD_NAME));
        return aggregationOperations;
    }

}
