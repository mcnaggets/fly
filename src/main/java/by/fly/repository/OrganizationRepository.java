package by.fly.repository;

import by.fly.model.Organization;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface OrganizationRepository extends MongoRepository<Organization, String>, QueryDslPredicateExecutor {
}
