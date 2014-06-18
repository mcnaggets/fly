package by.fly.model;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QHuman is a Querydsl query type for Human
 */
@Generated("com.mysema.query.codegen.EmbeddableSerializer")
public class QHuman extends BeanPath<Human> {

    private static final long serialVersionUID = -121167498L;

    public static final QHuman human = new QHuman("human");

    public final QAbstractModel _super = new QAbstractModel(this);

    public final StringPath address = createString("address");

    public final StringPath email = createString("email");

    //inherited
    public final StringPath id = _super.id;

    public final StringPath name = createString("name");

    //inherited
    public final BooleanPath new$ = _super.new$;

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

