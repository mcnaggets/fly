package by.fly.repository;

import by.fly.model.Customer;
import by.fly.model.OrderItem;
import by.fly.model.OrderStatus;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import java.time.LocalDateTime;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class OrderItemRepositoryTest extends AbstractBaseTest {

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Test
    public void testRepo() {
        Customer customer = createCustomer();

        OrderItem orderItem = new OrderItem(customer, LocalDateTime.now().plusDays(3));
        orderItemRepository.save(orderItem);

        assertTrue(orderItem.getId() != null);
        assertNotNull(orderItem.getCreatedAt());
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
            OrderItem orderItem = new OrderItem(customer, LocalDateTime.now().plusDays((long) (10 * Math.random())));
            orderItem.setStatus(OrderStatus.IN_PROGRESS);
            orderItemRepository.save(orderItem);
        }
    }

}
