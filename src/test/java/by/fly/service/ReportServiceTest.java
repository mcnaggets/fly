package by.fly.service;

import by.fly.model.Customer;
import by.fly.model.OrderItem;
import by.fly.model.OrderStatus;
import by.fly.model.QOrderItem;
import by.fly.repository.AbstractBaseTest;
import com.mongodb.BasicDBObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Random;

import static org.junit.Assert.assertTrue;

public class ReportServiceTest extends AbstractBaseTest {

    @Autowired
    ReportService reportService;

    @Autowired
    OrderService orderService;

    @Autowired
    CustomerService customerService;

    public void populateData() {
        final Random random = new Random();
        Customer customer = new Customer("Ivan", "+111220033");
        customerService.save(customer);
        for (int i = 0; i < 100; i++) {
            OrderItem orderItem = new OrderItem(LocalDateTime.now().plusDays((long) (10 * Math.random())));
            orderItem.setCustomer(customer);
            orderItem.setStatus(OrderStatus.READY);
            orderItem.setPrinterModel("HP" + random.nextInt(10));
            orderItem.setPrice(random.nextInt(1_000_000));
            orderService.save(orderItem);
        }
    }


    @Test
    public void testAggregation() {
        populateData();
        reportService.generateFacets(QOrderItem.orderItem.status.in(OrderStatus.READY, OrderStatus.PAID));
    }

    @Test
    public void testSum() {
        populateData();
        final float totalPrice = reportService.getTotalPrice(new BasicDBObject());
        assertTrue(totalPrice > 0);
    }

}
