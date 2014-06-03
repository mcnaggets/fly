package by.fly.service;

import by.fly.model.User;
import by.fly.repository.AbstractBaseTest;
import by.fly.repository.UserRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class UserServiceTest extends AbstractBaseTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    OrganizationService organizationService;

    @Test
    public void testBarcode() {
        User user = new User("fly", null, null, organizationService.getRootOrganization());
        user.setBarcode(userService.generateMasterBarcode());
        userRepository.save(user);
        assertThat(user.getBarcode(), notNullValue());
    }

}
