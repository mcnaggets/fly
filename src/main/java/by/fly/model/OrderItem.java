package by.fly.model;

import com.mysema.query.annotations.QueryEntity;
import com.mysema.query.annotations.QueryTransient;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@QueryEntity
@Document
public class OrderItem extends AbstractModel {

    @DBRef
    private Customer customer;

    @Indexed
    private Date createdAt;

    @Indexed
    private Date deadLine;

    @Indexed
    private String barcode;

    @Indexed(unique = true)
    private long orderNumber;

    private WorkType workType;

    private PrinterType printerType;

    @Indexed
    private String printerModel;

    private String description;

    private float price;

    private OrderStatus status = OrderStatus.CREATED;

    @PersistenceConstructor
    public OrderItem() {}

    public OrderItem(Customer customer, LocalDateTime deadLine) {
        setCustomer(customer);
        setDeadLine(deadLine);
        setCreatedAt(LocalDateTime.now());
    }
    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getPrinterModel() {
        return printerModel;
    }

    public void setPrinterModel(String printerModel) {
        this.printerModel = printerModel;
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

    @QueryTransient
    public LocalDateTime getCreatedAt() {
        return createdAt != null ? LocalDateTime.ofInstant(Instant.ofEpochMilli(createdAt.getTime()), ZoneId.systemDefault()) : null;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt != null ? Date.from(createdAt.atZone(ZoneId.systemDefault()).toInstant()) : null;
    }

    @QueryTransient
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

    public long getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(long orderNumber) {
        this.orderNumber = orderNumber;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public PrinterType getPrinterType() {
        return printerType;
    }

    public void setPrinterType(PrinterType printerType) {
        this.printerType = printerType;
    }

    public WorkType getWorkType() {
        return workType;
    }

    public void setWorkType(WorkType workType) {
        this.workType = workType;
    }
}
