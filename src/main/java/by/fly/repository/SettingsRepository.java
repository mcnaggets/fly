package by.fly.repository;

import by.fly.model.Settings;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface SettingsRepository extends MongoRepository<Settings, String>, QueryDslPredicateExecutor<Settings> {
}
