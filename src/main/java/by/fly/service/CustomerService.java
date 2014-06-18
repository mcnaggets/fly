package by.fly.service;

import by.fly.model.Customer;
import by.fly.model.QCustomer;
import by.fly.repository.CustomerRepository;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;

import java.util.List;

import static by.fly.util.Utils.containsIgnoreCasePattern;

@Service
public class CustomerService {

    private static final String CUSTOMER = QCustomer.customer.toString();
    private static final String CUSTOMER_NAME = QCustomer.customer.name.getMetadata().getName();
    private static final String CUSTOMER_PHONE = QCustomer.customer.phone.getMetadata().getName();

    @Autowired
    private MongoOperations mongoOperations;

    @Autowired
    private CustomerRepository customerRepository;

    public List<String> findCustomerNames(String filter) {
        DBObject query = BasicDBObjectBuilder.start(CUSTOMER_NAME, containsIgnoreCasePattern(filter)).get();
        return mongoOperations.execute(callback -> callback.getCollection(CUSTOMER).distinct(CUSTOMER_NAME, query));
    }

    public List<String> findCustomerPhones(String filter) {
        DBObject query = BasicDBObjectBuilder.start(CUSTOMER_PHONE, containsIgnoreCasePattern(filter)).get();
        return mongoOperations.execute(callback -> callback.getCollection(CUSTOMER).distinct(CUSTOMER_PHONE, query));
    }

    public Customer findByNameAndPhone(String clientName, String clientPhone) {
        return customerRepository.findOne(QCustomer.customer.name.eq(clientName).and(QCustomer.customer.phone.eq(clientPhone)));
    }

    public void save(Customer customer) {
        customerRepository.save(customer);
    }
}
