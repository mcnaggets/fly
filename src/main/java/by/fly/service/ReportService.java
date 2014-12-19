package by.fly.service;

import by.fly.model.OrderItem;
import com.mongodb.DBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class ReportService {

    public static final String COUNT_FIELD_NAME = "count";

    public static class ValueCount {
        private String value;
        private int count;

        public ValueCount(String value, int count) {
            this.value = value;
            this.count = count;
        }

        public int getCount() {
            return count;
        }

        public String getValue() {
            return value;
        }
    }

    @Autowired
    private MongoOperations mongoOperations;

    public List<ValueCount> getOrderFieldValueCounts(String taqName) {
        TypedAggregation<OrderItem> agg = new TypedAggregation<>(OrderItem.class,
                project(taqName),
//                unwind(taqName),
                group(taqName).count().as(COUNT_FIELD_NAME),
                project(COUNT_FIELD_NAME).and(taqName).previousOperation(),
                sort(DESC, COUNT_FIELD_NAME)
        );
        AggregationResults<DBObject> results = mongoOperations.aggregate(agg, DBObject.class);
        return results.getMappedResults().stream().map(obj -> new ValueCount((String) obj.get(taqName),
                (Integer) obj.get(COUNT_FIELD_NAME))).collect(Collectors.toList());
    }

}
