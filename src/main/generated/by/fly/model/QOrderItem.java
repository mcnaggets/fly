package by.fly.model;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;


/**
 * QOrderItem is a Querydsl query type for OrderItem
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QOrderItem extends EntityPathBase<OrderItem> {

    private static final long serialVersionUID = 371168074L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrderItem orderItem = new QOrderItem("orderItem");

    public final QAbstractModel _super = new QAbstractModel(this);

    public final StringPath additionalWork = createString("additionalWork");

    public final StringPath barcode = createString("barcode");

    public final StringPath clientName = createString("clientName");

    public final StringPath clientPhone = createString("clientPhone");

    public final DateTimePath<java.util.Date> createdAt = createDateTime("createdAt", java.util.Date.class);

    public final QCustomer customer;

    public final DateTimePath<java.util.Date> deadLine = createDateTime("deadLine", java.util.Date.class);

    public final StringPath description = createString("description");

    //inherited
    public final StringPath id = _super.id;

    public final StringPath itemType = createString("itemType");

    public final QUser master;

    //inherited
    public final BooleanPath new$ = _super.new$;

    public final StringPath orderCode = createString("orderCode");

    public final NumberPath<Long> orderNumber = createNumber("orderNumber", Long.class);

    public final NumberPath<Float> price = createNumber("price", Float.class);

    public final StringPath printerModel = createString("printerModel");

    public final EnumPath<OrderStatus> status = createEnum("status", OrderStatus.class);

    public final BooleanPath test = createBoolean("test");

    public final SetPath<WorkType, EnumPath<WorkType>> workTypes = this.<WorkType, EnumPath<WorkType>>createSet("workTypes", WorkType.class, EnumPath.class, PathInits.DIRECT2);

    public QOrderItem(String variable) {
        this(OrderItem.class, forVariable(variable), INITS);
    }

    public QOrderItem(Path<? extends OrderItem> path) {
        this(path.getType(), path.getMetadata(), path.getMetadata().isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QOrderItem(PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QOrderItem(PathMetadata<?> metadata, PathInits inits) {
        this(OrderItem.class, metadata, inits);
    }

    public QOrderItem(Class<? extends OrderItem> type, PathMetadata<?> metadata, PathInits inits) {
        super(type, metadata, inits);
        this.customer = inits.isInitialized("customer") ? new QCustomer(forProperty("customer")) : null;
        this.master = inits.isInitialized("master") ? new QUser(forProperty("master"), inits.get("master")) : null;
    }

}

