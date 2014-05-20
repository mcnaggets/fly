package by.fly.repository;

import by.fly.model.QUser;
import by.fly.model.User;
import by.fly.service.OrganizationService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

public class UserRepositoryTest extends AbstractBaseTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    OrganizationService organizationService;

    @Test
    public void testRepo() {
        String login = String.valueOf(System.currentTimeMillis());
        String name = "Валера";

        User user = new User(login, null, login, organizationService.getRootOrganization());
        user.setName(name);
        userRepository.save(user);

        Iterable<User> users = userRepository.findAll(QUser.user.name.eq(name).and(QUser.user.login.eq(login)));
        assertThat(users, hasItem(user));
    }

}
