package by.fly.repository;

import by.fly.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface UserRepository extends JpaRepository<User, Long>, QueryDslPredicateExecutor {
    User findByBarcode(String barcode);
}
