package by.fly.repository;

import by.fly.model.Organization;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrganizationRepository extends MongoRepository<Organization, String> {
}
