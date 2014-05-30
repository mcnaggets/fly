package by.fly.service;

import by.fly.model.Customer;
import by.fly.model.QCustomer;
import by.fly.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    public static final String CUSTOMER = QCustomer.customer.toString();
    public static final String CUSTOMER_NAME = QCustomer.customer.name.getMetadata().getName();
    public static final String CUSTOMER_PHONE = QCustomer.customer.phone.getMetadata().getName();

    @Autowired
    private MongoOperations mongoOperations;

    @Autowired
    private CustomerRepository customerRepository;

    public List<String> findCustomerNames() {
        return mongoOperations.execute(callback -> callback.getCollection(CUSTOMER).distinct(CUSTOMER_NAME));
    }

    public List<String> findCustomerPhones() {
        return mongoOperations.execute(callback -> callback.getCollection(CUSTOMER).distinct(CUSTOMER_PHONE));
    }

    public Customer findByNameAndPhone(String clientName, String clientPhone) {
        return customerRepository.findOne(QCustomer.customer.name.eq(clientName).and(QCustomer.customer.phone.eq(clientPhone)));
    }

    public void save(Customer customer) {
        customerRepository.save(customer);
    }
}
