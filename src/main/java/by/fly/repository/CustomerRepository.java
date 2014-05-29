package by.fly.repository;

import by.fly.model.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface CustomerRepository extends MongoRepository<Customer, String>, QueryDslPredicateExecutor<Customer> {
}
