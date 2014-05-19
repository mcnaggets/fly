package by.fly.config;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Configuration
@EnableJpaRepositories(basePackages = {"by.fly.repository"})
@EnableTransactionManagement
public class PersistenceConfig {

    @Value("${databaseDriver}")
    private String databaseDriver;
    @Value("${databaseUrl}")
    private String databaseUrl;
    @Value("${databaseUser}")
    private String databaseUser;
    @Value("${databasePassword}")
    private String databasePassword;

    @Bean
    public DataSource flyDataSource() {
        final BasicDataSource basicDataSource = new BasicDataSource();

        basicDataSource.setDriverClassName(this.databaseDriver);
        basicDataSource.setUrl(this.databaseUrl);
        basicDataSource.setUsername(this.databaseUser);
        basicDataSource.setPassword(this.databasePassword);
        //CONNECTIONPOOL SETTINGS
        basicDataSource.setTestWhileIdle(true);
        basicDataSource.setTestOnBorrow(true);
        basicDataSource.setTestOnReturn(false);
        basicDataSource.setValidationQuery("select 1 from dual");
        //missig: validationInterval="30000"
        basicDataSource.setTimeBetweenEvictionRunsMillis(30000);
        basicDataSource.setMaxActive(20);
        basicDataSource.setMaxIdle(5);
        basicDataSource.setMaxWait(10000L);
        basicDataSource.setInitialSize(5);
        basicDataSource.setRemoveAbandonedTimeout(600);
        basicDataSource.setRemoveAbandoned(false);
        basicDataSource.setLogAbandoned(true);
        basicDataSource.setMinEvictableIdleTimeMillis(30000L);
//      basicDataSource.setDefaultAutoCommit(false);


        return basicDataSource;
    }

    private Properties getJpaProperties() throws IOException {
        final Properties jpaProperties = new Properties();
        final ClassPathResource resource = new ClassPathResource("hibernate.properties");
        final InputStream inputStream = resource.getInputStream();

        jpaProperties.load(inputStream);

        return jpaProperties;
    }

    @Bean
    public HibernateJpaVendorAdapter hibernateJpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

    @Bean
    public EntityManagerFactory entityManagerFactory() throws IOException {
        final LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactory.setDataSource(this.flyDataSource());
        entityManagerFactory.setJpaVendorAdapter(this.hibernateJpaVendorAdapter());

        entityManagerFactory.setPackagesToScan("by.fly.model");
        entityManagerFactory.afterPropertiesSet();

        final Properties jpaProperties = this.getJpaProperties();
        entityManagerFactory.setJpaProperties(jpaProperties);

        return entityManagerFactory.getObject();
    }


    @Bean
    public PlatformTransactionManager transactionManager() throws IOException {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(this.entityManagerFactory());
        return transactionManager;
    }


}
