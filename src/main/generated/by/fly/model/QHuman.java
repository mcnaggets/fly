package by.fly.model;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QHuman is a Querydsl query type for Human
 */
@Generated("com.mysema.query.codegen.SupertypeSerializer")
public class QHuman extends EntityPathBase<Human> {

    private static final long serialVersionUID = -121167498L;

    public static final QHuman human = new QHuman("human");

    public final org.springframework.data.jpa.domain.QAbstractPersistable _super = new org.springframework.data.jpa.domain.QAbstractPersistable(this);

    public final StringPath address = createString("address");

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final StringPath phone = createString("phone");

    public QHuman(String variable) {
        super(Human.class, forVariable(variable));
    }

    public QHuman(Path<? extends Human> path) {
        super(path.getType(), path.getMetadata());
    }

    public QHuman(PathMetadata<?> metadata) {
        super(Human.class, metadata);
    }

}

