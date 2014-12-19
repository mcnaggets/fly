package by.fly.service;

import by.fly.model.QOrderItem;
import by.fly.model.statistics.DailyOrders;
import by.fly.model.statistics.QDailyOrders;
import by.fly.repository.DailyOrdersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;

import static by.fly.util.Utils.readyOrdersCriteria;
import static by.fly.util.Utils.sortByOrderDeadLine;
import static org.springframework.data.mongodb.core.mapreduce.MapReduceOptions.options;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
public class DailyOrdersService {

    @Autowired
    private MongoOperations mongoOperations;

    @Autowired
    private DailyOrdersRepository dailyOrdersRepository;

    public long count() {
        return mongoOperations.count(null, DailyOrders.class);
    }

    public void refreshData() {
        mongoOperations.mapReduce(
                query(readyOrdersCriteria()).limit(100_000).with(sortByOrderDeadLine()),
                QOrderItem.orderItem.toString(), "classpath:js/daily-orders-map.js", "classpath:js/daily-orders-reduce.js",
                options().outputTypeReplace().outputCollection(QDailyOrders.dailyOrders.toString()), DailyOrders.class);

    }

    public Page<DailyOrders> findAll(Pageable pageable) {
        return dailyOrdersRepository.findAll(pageable);
    }
}
