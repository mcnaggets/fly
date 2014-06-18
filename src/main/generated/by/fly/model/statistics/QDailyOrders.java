package by.fly.model.statistics;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QDailyOrders is a Querydsl query type for DailyOrders
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QDailyOrders extends EntityPathBase<DailyOrders> {

    private static final long serialVersionUID = -818087766L;

    public static final QDailyOrders dailyOrders = new QDailyOrders("dailyOrders");

    public final DateTimePath<java.util.Date> date = createDateTime("date", java.util.Date.class);

    public final NumberPath<Integer> paidCount = createNumber("paidCount", Integer.class);

    public final NumberPath<Float> price = createNumber("price", Float.class);

    public final NumberPath<Integer> readyCount = createNumber("readyCount", Integer.class);

    public QDailyOrders(String variable) {
        super(DailyOrders.class, forVariable(variable));
    }

    public QDailyOrders(Path<? extends DailyOrders> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDailyOrders(PathMetadata<?> metadata) {
        super(DailyOrders.class, metadata);
    }

}

