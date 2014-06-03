package by.fly.repository;

import by.fly.model.Organization;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

public class OrganizationRepositoryTest extends AbstractBaseTest {

    @Autowired
    OrganizationRepository organizationRepository;

    @Test
    @Rollback(false)
    public void createDefaultOrganization() {
        Organization organization = new Organization("fly");
        organization.setUnp("111");
        organizationRepository.save(organization);
    }

}
