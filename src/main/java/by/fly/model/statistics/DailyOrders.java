package by.fly.model.statistics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.Id;

import java.util.Date;

public class DailyOrders {

    @Id
    private Date date;

    private float price;
    private int readyCount;
    private int paidCount;

    public DailyOrders(Date date,
                       @Value("#root.value.readyCount") int readyCount,
                       @Value("#root.value.paidCount") int paidCount,
                       @Value("#root.value.price") float price) {
        this.date = date;
        this.readyCount = readyCount;
        this.paidCount = paidCount;
        this.price = price;
    }

    public float getPrice() {
        return price;
    }

    public int getReadyCount() {
        return readyCount;
    }

    public int getPaidCount() {
        return paidCount;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "DailyOrders{" +
                "date=" + date +
                ", price=" + price +
                ", readyCount=" + readyCount +
                ", paidCount=" + paidCount +
                '}';
    }
}
