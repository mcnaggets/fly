package by.fly.service;

import by.fly.model.QOrderItem;
import by.fly.model.facet.OrderItemFacets;
import by.fly.model.facet.ValueCount;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mysema.query.mongodb.MongodbSerializer;
import com.mysema.query.types.Path;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.path.CollectionPathBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class ReportService {

    public static final String COUNT_FIELD_NAME = "count";
    public static final String SUM_FIELD_NAME = "sum";
    public static final String ID = "_id";
    public static final String ORDER_ITEM_COLLECTION = QOrderItem.orderItem.getMetadata().getName();
    public static final String ORDER_ITEM_PRICE = QOrderItem.orderItem.price.getMetadata().getName();

    public static final String $_MATCH = "$match";
    public static final String $_GROUP = "$group";
    public static final String $_SUM = "$sum";
    public static final String $_PROJECT = "$project";
    public static final String $_UNWIND = "$unwind";
    public static final String $_ID = "$_id";
    public static final String $_SORT = "$sort";

    @Autowired
    private MongoOperations mongoOperations;

    public OrderItemFacets generateFacets(Predicate predicate) {
        DBObject filter = (DBObject) predicate.accept(new MongodbSerializer() {
            @Override
            protected DBObject asDBObject(String key, Object value) {
                return super.asDBObject(key, mongoOperations.getConverter().convertToMongoType(value));
            }
        }, null);
        OrderItemFacets facets = new OrderItemFacets();
        facets.setTotalPrice(getTotalPrice(filter));
        for (Path path : OrderItemFacets.AVAILABLE_PROPERTIES) {
            facets.addFacets(path, getFacets(path, filter));
        }
        return facets;
    }

    public float getTotalPrice(DBObject filter) {
        final AggregationOutput output = mongoOperations.execute(call -> call.getCollection(ORDER_ITEM_COLLECTION).aggregate(
                Stream.of(
                        new BasicDBObject($_MATCH, filter),
                        new BasicDBObject($_GROUP,
                                BasicDBObjectBuilder.start()
                                        .add(ID, "null")
                                        .add(SUM_FIELD_NAME, new BasicDBObject($_SUM, "$" + ORDER_ITEM_PRICE)).get())
                ).collect(Collectors.toList())
        ));
        return StreamSupport.stream(output.results().spliterator(), false)
                .map(dbo -> ((Number) dbo.get(SUM_FIELD_NAME)).floatValue()).reduce(0f, Float::max);
    }

    private List<ValueCount> getFacets(Path path, DBObject filter) {
        final String tagName = path.getMetadata().getName();
        final AggregationOutput output = mongoOperations.execute(call ->
                call.getCollection(ORDER_ITEM_COLLECTION).aggregate(facetsPipeline(filter, path)));
        return StreamSupport.stream(output.results().spliterator(), false)
                .map(obj -> new ValueCount((String) obj.get(tagName), (Integer) obj.get(COUNT_FIELD_NAME))).collect(Collectors.toList());
    }

    private List<DBObject> facetsPipeline(DBObject filter, Path path) {
        final String tagName = path.getMetadata().getName();
        List<DBObject> objects = new LinkedList<>();
        objects.add(new BasicDBObject($_MATCH, filter));
        objects.add(new BasicDBObject($_PROJECT, new BasicDBObject(tagName, 1)));
        if (path instanceof CollectionPathBase) {
            objects.add(new BasicDBObject($_UNWIND, "$" + tagName));
        }
        objects.add(new BasicDBObject($_GROUP,
                BasicDBObjectBuilder.start()
                        .add(ID, "$" + tagName)
                        .add(COUNT_FIELD_NAME, new BasicDBObject($_SUM, 1)).get()));
        objects.add(new BasicDBObject($_PROJECT,
                BasicDBObjectBuilder.start()
                        .add(COUNT_FIELD_NAME, 1)
                        .add(ID, 0)
                        .add(tagName, $_ID).get()));
        objects.add(new BasicDBObject($_SORT, new BasicDBObject(COUNT_FIELD_NAME, -1)));
        return objects;
    }

}
