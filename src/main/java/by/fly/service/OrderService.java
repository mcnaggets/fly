package by.fly.service;

import by.fly.model.OrderItem;
import by.fly.model.OrderStatus;
import by.fly.model.QOrderItem;
import by.fly.repository.OrderItemRepository;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.expr.BooleanExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;

import java.util.List;

import static by.fly.util.Utils.containsIgnoreCasePattern;

@Service
public class OrderService {

    public static final String ORDER_ITEM = QOrderItem.orderItem.toString();
    public static final String ORDER_NUMBER_PATH = QOrderItem.orderItem.orderCode.toString();
    public static final String PRINTER_MODEL = QOrderItem.orderItem.printerModel.getMetadata().getName();
    public static final String CREATED_AT = QOrderItem.orderItem.createdAt.getMetadata().getName();

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private SequenceService sequenceService;

    @Autowired
    private MongoOperations mongoOperations;

    public void save(OrderItem orderItem) {
        orderItemRepository.save(orderItem);
    }

    public Page<OrderItem> findAll(Predicate filterPredicate, Pageable pageable) {
        return orderItemRepository.findAll(filterPredicate, pageable);
    }

    public OrderItem findOne(Predicate filterPredicate) {
        return orderItemRepository.findOne(filterPredicate);
    }

    public long count(Predicate filterPredicate) {
        return orderItemRepository.count(filterPredicate);
    }

    public List<String> findPrinterModels(String filter) {
        DBObject query = BasicDBObjectBuilder.start(PRINTER_MODEL, containsIgnoreCasePattern(filter)).get();
        return mongoOperations.execute(db -> db.getCollection(ORDER_ITEM).distinct(PRINTER_MODEL, query));
    }

    public long getNexOrderNumber() {
        return sequenceService.getNextSequence(ORDER_NUMBER_PATH);
    }

    public OrderItem findInProgressItemByBarcode(String barcode) {
        return findOne(QOrderItem.orderItem.barcode.eq(barcode).and(QOrderItem.orderItem.status.eq(OrderStatus.IN_PROGRESS)));
    }

    public OrderItem findLastItemByBarcode(String barcode) {
        return orderItemRepository.findAll(
                QOrderItem.orderItem.barcode.eq(barcode),
                new PageRequest(0, 1, Sort.Direction.DESC, CREATED_AT)).getContent().stream().findFirst().orElse(null);
    }

    public List<OrderItem> findAll(Predicate predicate) {
        return (List<OrderItem>) orderItemRepository.findAll(predicate);
    }

}
