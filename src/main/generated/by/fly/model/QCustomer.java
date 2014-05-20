package by.fly.model;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QCustomer is a Querydsl query type for Customer
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QCustomer extends EntityPathBase<Customer> {

    private static final long serialVersionUID = -1863061771L;

    public static final QCustomer customer = new QCustomer("customer");

    public final QHuman _super = new QHuman(this);

    //inherited
    public final StringPath address = _super.address;

    //inherited
    public final StringPath email = _super.email;

    //inherited
    public final StringPath id = _super.id;

    //inherited
    public final StringPath name = _super.name;

    //inherited
    public final StringPath phone = _super.phone;

    public QCustomer(String variable) {
        super(Customer.class, forVariable(variable));
    }

    public QCustomer(Path<? extends Customer> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCustomer(PathMetadata<?> metadata) {
        super(Customer.class, metadata);
    }

}

