package by.fly.repository;

import by.fly.model.statistics.DailyOrders;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import java.util.Date;

public interface DailyOrdersRepository extends MongoRepository<DailyOrders, Date>, QueryDslPredicateExecutor<DailyOrders> {
}
