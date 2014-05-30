package by.fly.repository;

import by.fly.model.OrderItem;
import by.fly.model.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import java.util.List;

public interface OrderItemRepository extends MongoRepository<OrderItem, String>, QueryDslPredicateExecutor<OrderItem> {

    List<OrderItem> findByStatus(OrderStatus status, Pageable pageable);

    List<OrderItem> findByStatus(OrderStatus status, Sort sort);

}
