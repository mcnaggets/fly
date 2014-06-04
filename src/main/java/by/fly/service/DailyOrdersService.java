package by.fly.service;

import by.fly.model.OrderStatus;
import by.fly.model.QOrderItem;
import by.fly.model.statistics.DailyOrders;
import by.fly.model.statistics.QDailyOrders;
import by.fly.repository.DailyOrdersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

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
                query(Criteria.where(QOrderItem.orderItem.status.getMetadata().getName()).in(OrderStatus.READY.name(), OrderStatus.PAID.name()))
                        .limit(100_000).with(new Sort(Sort.Direction.DESC, QOrderItem.orderItem.deadLine.getMetadata().getName())),
                QOrderItem.orderItem.toString(), "classpath:js/daily-orders-map.js", "classpath:js/daily-orders-reduce.js",
                options().outputTypeReplace().outputCollection(QDailyOrders.dailyOrders.toString()), DailyOrders.class);

    }

    public Page<DailyOrders> findAll(Pageable pageable) {
        return dailyOrdersRepository.findAll(pageable);
    }
}
