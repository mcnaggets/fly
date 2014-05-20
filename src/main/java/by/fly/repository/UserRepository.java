package by.fly.repository;

import by.fly.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface UserRepository extends MongoRepository<User, Long>, QueryDslPredicateExecutor {
    User findByBarcode(String barcode);
}
