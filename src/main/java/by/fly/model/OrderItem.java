package by.fly.model;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Entity
@Table(name = "FLY_ORDER")
public class OrderItem extends AbstractPersistable<Long> {

    @ManyToOne(optional = false)
    private Customer customer;

    private Date createdAt;

    private Date deadLine;

    private String description;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.CREATED;

    OrderItem() {
        // for reflection
    }

    public OrderItem(Customer customer, LocalDateTime deadLine) {
        setCustomer(customer);
        setDeadLine(deadLine);
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getDescription() {
        return description;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt != null ? LocalDateTime.ofInstant(Instant.ofEpochMilli(createdAt.getTime()), ZoneId.systemDefault()) : null;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt != null ? Date.from(createdAt.atZone(ZoneId.systemDefault()).toInstant()) : null;
    }

    public LocalDateTime getDeadLine() {
        return deadLine != null ? LocalDateTime.ofInstant(Instant.ofEpochMilli(deadLine.getTime()), ZoneId.systemDefault()) : null;
    }

    public void setDeadLine(LocalDateTime deadLine) {
        this.deadLine = deadLine != null ? Date.from(deadLine.atZone(ZoneId.systemDefault()).toInstant()) : null;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    @PrePersist
    public void prePersist() {
        setCreatedAt(LocalDateTime.now());
    }

}
