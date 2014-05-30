package by.fly.service;

import by.fly.model.OrderItem;
import by.fly.model.QOrderItem;
import by.fly.repository.OrderItemRepository;
import com.mysema.query.types.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public Page<OrderItem> findAll(Predicate filterPredicate, PageRequest pageRequest) {
        return orderItemRepository.findAll(filterPredicate, pageRequest);
    }

    public OrderItem findOne(Predicate filterPredicate) {
        return orderItemRepository.findOne(filterPredicate);
    }

    public long count(Predicate filterPredicate) {
        return orderItemRepository.count(filterPredicate);
    }

    public List<String> findPrinterModels() {
        return mongoOperations.execute(callback -> callback.getCollection(ORDER_ITEM).distinct(PRINTER_MODEL));
    }

    public long getNexOrderNumber() {
        return sequenceService.getNextSequence(ORDER_NUMBER_PATH);
    }

    public OrderItem findLastItemByBarcode(String barcode) {
        return orderItemRepository.findAll(
                QOrderItem.orderItem.barcode.eq(barcode),
                new PageRequest(0, 1, Sort.Direction.DESC, CREATED_AT)).getContent().stream().findFirst().orElse(null);
    }
}
