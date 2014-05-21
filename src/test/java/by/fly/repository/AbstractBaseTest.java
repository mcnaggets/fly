package by.fly.repository;

import by.fly.config.ApplicationConfig;
import by.fly.config.MongoConfig;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.PostConstruct;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationConfig.class})
@ActiveProfiles("test")
public abstract class AbstractBaseTest {

    @Autowired
    MongoOperations mongoOperations;

    @Configuration
    static class TestMongoConfig extends MongoConfig {

        @Autowired
        MongoDbFactory mongoDbFactory;

        @Override
        protected String getDatabaseName() {
            return super.getDatabaseName() + "-test";
        }

        @PostConstruct
        void postConstruct() {
            mongoDbFactory.getDb().dropDatabase();
        }

    }

}
