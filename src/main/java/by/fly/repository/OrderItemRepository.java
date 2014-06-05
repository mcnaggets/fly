package by.fly.repository;

import by.fly.model.OrderItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface OrderItemRepository extends MongoRepository<OrderItem, String>, QueryDslPredicateExecutor<OrderItem> {

}
