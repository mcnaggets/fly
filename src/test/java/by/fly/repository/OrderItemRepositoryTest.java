package by.fly.repository;

import by.fly.model.Customer;
import by.fly.model.OrderItem;
import by.fly.model.OrderStatus;
import by.fly.model.statistics.DailyOrders;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapreduce.MapReduceResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.annotation.Rollback;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.*;
import static org.springframework.data.mongodb.core.mapreduce.MapReduceOptions.options;

public class OrderItemRepositoryTest extends AbstractBaseTest {

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @Test
    public void testRepo() {
        Customer customer = createCustomer();

        OrderItem orderItem = new OrderItem(LocalDateTime.now().plusDays(3));
        orderItem.setCustomer(customer);
        orderItemRepository.save(orderItem);

        assertTrue(orderItem.getId() != null);
        assertNotNull(orderItem.getCreatedAt());
    }

    @Test
    public void testMapReduce() {
        for (int i = 0; i < 1000; i++) {
            OrderItem orderItem = new OrderItem(LocalDateTime.now().plusDays((long) (10 * Math.random())));
            orderItem.setOrderNumber(System.nanoTime());
            orderItem.setPrice((float) (1000 * Math.random()));
            orderItem.setStatus(Math.random() > 0.5 ? OrderStatus.READY : OrderStatus.PAID);
            orderItemRepository.save(orderItem);
        }

        MapReduceResults<DailyOrders> dailyOrders = mongoOperations.mapReduce(
                new Query(Criteria.where("status").in(OrderStatus.READY.name(), OrderStatus.PAID.name())),
                "orderItem", "classpath:js/daily-orders-map.js", "classpath:js/daily-orders-reduce.js",
                options().outputCollection("jmr1_out"), DailyOrders.class);
        dailyOrders.forEach(System.out::println);
    }

    @Test
    public void testDistinct() {
        Customer customer = createCustomer();
        List<String> execute = mongoOperations.execute(call -> call.getCollection("customer").distinct("name"));
        assertThat(execute, hasItem(customer.getName()));
    }

    @Test
    public void testDBRefSearch() {
        Customer customer = createCustomer();

        OrderItem orderItem = new OrderItem(LocalDateTime.now().plusDays(3));
        orderItem.setCustomer(customer);
        orderItemRepository.save(orderItem);

        Query query = new Query(Criteria.where("customer.$id").is(customer.getName()));
        List<OrderItem> items = mongoOperations.find(query, OrderItem.class);
        assertThat(items, hasItem(orderItem));
    }

    private Customer createCustomer() {
        Customer customer = new Customer("Валера", "+375297861213");
        customerRepository.save(customer);
        return customer;
    }

    @Test
    @Rollback(false)
    public void populateData() {
        Customer customer = createCustomer();
        for (int i = 0; i < 100; i++) {
            OrderItem orderItem = new OrderItem(LocalDateTime.now().plusDays((long) (10 * Math.random())));
            orderItem.setCustomer(customer);
            orderItem.setStatus(OrderStatus.IN_PROGRESS);
            orderItemRepository.save(orderItem);
        }
    }

}
