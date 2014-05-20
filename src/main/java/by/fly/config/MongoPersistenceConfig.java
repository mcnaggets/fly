package by.fly.config;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories(basePackages = {"by.fly.repository"})
public class MongoPersistenceConfig extends AbstractMongoConfiguration {

    @Value("${mongo.db.name}")
    private String mongoDBName;
    @Value("${mongo.host}")
    private String mongoHost;
    @Value("${mongo.port}")
    private int mongoPort;

    @Override
    protected String getDatabaseName() {
        return mongoDBName;
    }

    @Override
    public String getMappingBasePackage() {
        return "by.fly.model";
    }

    @Override
    public Mongo mongo() throws Exception {
        return new MongoClient(mongoHost, mongoPort);
    }

    @Bean
    public GridFsTemplate gridFsTemplate() throws Exception {
        return new GridFsTemplate(mongoDbFactory(), mappingMongoConverter());
    }
}
