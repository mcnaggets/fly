package by.fly.repository;

import by.fly.config.ApplicationConfig;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationConfig.class})
//@TransactionConfiguration(defaultRollback = true)
//@Transactional
public abstract class AbstractBaseTest {
}
