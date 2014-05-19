package by.fly.repository;

import by.fly.model.QUser;
import by.fly.model.User;
import by.fly.service.OrganizationService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

public class UserRepositoryTest extends AbstractBaseTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    JpaRepository<User, Long> userJpaRepository;

    @Autowired
    OrganizationService organizationService;

    @Test
    public void testRepo() {
        String login = String.valueOf(System.currentTimeMillis());
        String name = "Валера";

        User user = new User(login, null, login, organizationService.getRootOrganization());
        user.setName(name);
        userJpaRepository.save(user);

        Iterable<User> users = userRepository.findAll(QUser.user.name.eq(name).and(QUser.user.login.eq(login)));
        assertThat(users, hasItem(user));
    }

}
