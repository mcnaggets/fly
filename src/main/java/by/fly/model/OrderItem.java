package by.fly.model;

import com.mysema.query.annotations.QueryEntity;
import com.mysema.query.annotations.QueryTransient;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@QueryEntity
@Document
public class OrderItem extends AbstractModel {

    public static final String ORDER_CODE_PREFIX = "*1Z";

    @DBRef
    private Customer customer;

    @Indexed
    private Date createdAt;

    @Indexed
    private Date deadLine;

    @Indexed
    private String barcode;

    @Indexed
    private long orderNumber;

    @Indexed
    private String orderCode;

    private Set<WorkType> workTypes = new HashSet<>();

    private String itemType;

    @Indexed
    private String printerModel;

    private String description;

    private float price;

    private OrderStatus status = OrderStatus.CREATED;

    @Indexed
    private String clientPhone;

    @Indexed
    private String clientName;

    private boolean test;

    @DBRef
    private User master;

    private String additionalWork;

    @PersistenceConstructor
    public OrderItem(long orderNumber) {
        this.orderNumber = orderNumber;
    }

    public OrderItem(LocalDateTime deadLine) {
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
        this.clientName = customer.getName();
        this.clientPhone = customer.getPhone();
        this.customer = customer;
    }

    @QueryTransient
    public LocalDateTime getCreatedAt() {
        return createdAt != null ? LocalDateTime.ofInstant(createdAt.toInstant(), ZoneId.systemDefault()) : null;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt != null ? Date.from(createdAt.atZone(ZoneId.systemDefault()).toInstant()) : null;
    }

    @QueryTransient
    public LocalDateTime getDeadLine() {
        return deadLine != null ? LocalDateTime.ofInstant(deadLine.toInstant(), ZoneId.systemDefault()) : null;
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
        setOrderCode(ORDER_CODE_PREFIX + orderNumber);
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public Set<WorkType> getWorkTypes() {
        return workTypes;
    }

    public boolean addWorkType(WorkType workType) {
        return workTypes.add(workType);
    }

    public boolean removeWorkType(WorkType workType) {
        return workTypes.remove(workType);
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getClientName() {
        return clientName;
    }

    public String getClientPhone() {
        return clientPhone;
    }

    public boolean isTest() {
        return test;
    }

    public User getMaster() {
        return master;
    }

    public String getAdditionalWork() {
        return additionalWork;
    }

    public void setTest(boolean test) {
        this.test = test;
    }

    public void setMaster(User master) {
        this.master = master;
    }

    public void setAdditionalWork(String additionalWork) {
        this.additionalWork = additionalWork;
    }

    @QueryTransient
    public String getWorkTypeMessages(String delimiter) {
        return workTypes.stream().map(WorkType::getMessage).collect(Collectors.joining(delimiter));
    }

    public boolean containsWorkType(WorkType workType) {
        return workTypes.contains(workType);
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "createdAt=" + createdAt +
                ", deadLine=" + deadLine +
                ", barcode='" + barcode + '\'' +
                ", orderNumber=" + orderNumber +
                ", orderCode='" + orderCode + '\'' +
                ", workTypes=" + getWorkTypeMessages(", ") +
                ", itemType=" + itemType +
                ", printerModel='" + printerModel + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", status=" + status +
                ", clientPhone='" + clientPhone + '\'' +
                ", clientName='" + clientName + '\'' +
                '}';
    }

}
