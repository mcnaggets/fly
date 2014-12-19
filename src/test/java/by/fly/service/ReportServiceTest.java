package by.fly.service;

import by.fly.model.Customer;
import by.fly.model.OrderItem;
import by.fly.model.OrderStatus;
import by.fly.model.QOrderItem;
import by.fly.repository.AbstractBaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import java.time.LocalDateTime;
import java.util.Random;

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
            orderService.save(orderItem);
        }
    }


    @Test
    @Rollback(false)
    public void testAggregation() {
        populateData();
        reportService.getOrderFieldValueCounts(QOrderItem.orderItem.printerModel.getMetadata().getName());
    }

}
