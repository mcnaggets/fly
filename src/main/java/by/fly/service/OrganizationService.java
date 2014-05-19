package by.fly.service;

import by.fly.model.Organization;
import by.fly.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrganizationService {

    @Autowired
    private OrganizationRepository repository;

    public Organization getRootOrganization() {
        if (repository.count() > 0) {
            return repository.findAll().iterator().next();
        }
        Organization organization = new Organization("ЦЗК");
        return repository.save(organization);
    }

}
