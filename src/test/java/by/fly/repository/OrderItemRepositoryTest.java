package by.fly.repository;

import by.fly.model.*;
import by.fly.model.statistics.DailyOrders;
import by.fly.model.statistics.QDailyOrders;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapreduce.MapReduceResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.annotation.Rollback;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

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
    public void testMapReduce() throws InterruptedException {
        long time = System.currentTimeMillis();
        Executors.newFixedThreadPool(4).invokeAll(Arrays.asList(
                (Callable<Object>) this::createManyOrders,
                this::createManyOrders,
                this::createManyOrders,
                this::createManyOrders
        ));
        System.out.println("create time: " + (System.currentTimeMillis() - time));

        time = System.currentTimeMillis();
        MapReduceResults<DailyOrders> dailyOrders = mongoOperations.mapReduce(
                new Query(Criteria.where("status").in(OrderStatus.READY.name(), OrderStatus.PAID.name())),
                "orderItem", "classpath:js/daily-orders-map.js", "classpath:js/daily-orders-reduce.js",
                options().outputTypeReplace().outputCollection(QDailyOrders.dailyOrders.toString()), DailyOrders.class);
        System.out.println("mapreduce time: " + (System.currentTimeMillis() - time));
        dailyOrders.getCounts();
    }

    private Void createManyOrders() {
        List<OrderItem> batch = new LinkedList<>();
        for (int i = 0; i < 250_000; i++) {
            OrderItem orderItem = new OrderItem(LocalDateTime.now().plusDays((long) (50 * Math.random())));
            orderItem.setOrderNumber(System.nanoTime());
            orderItem.setPrinterType(Math.random() > 0.5 ? PrinterType.JET : PrinterType.LASER);
            orderItem.addWorkType(WorkType.values()[((int) (System.nanoTime() % 3))]);
            orderItem.setPrice((float) (1000 * Math.random()));
            orderItem.setStatus(Math.random() > 0.5 ? OrderStatus.READY : OrderStatus.PAID);
            batch.add(orderItem);
            if (batch.size() == 100) {
                orderItemRepository.save(batch);
                batch.clear();
            }
        }
        return null;
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
