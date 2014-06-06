package by.fly.model.statistics;

import com.mysema.query.annotations.QueryEntity;
import com.mysema.query.annotations.QueryTransient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Document
@QueryEntity
public class DailyOrders {

    @Id
    private final Date date;

    private final float price;
    private final int readyCount;
    private final int paidCount;

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

    @QueryTransient
    public LocalDate getDate() {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalDate();
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
