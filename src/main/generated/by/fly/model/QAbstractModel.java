package by.fly.model;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QAbstractModel is a Querydsl query type for AbstractModel
 */
@Generated("com.mysema.query.codegen.EmbeddableSerializer")
public class QAbstractModel extends BeanPath<AbstractModel> {

    private static final long serialVersionUID = -545503376L;

    public static final QAbstractModel abstractModel = new QAbstractModel("abstractModel");

    public final StringPath id = createString("id");

    public final BooleanPath new$ = createBoolean("new");

    public QAbstractModel(String variable) {
        super(AbstractModel.class, forVariable(variable));
    }

    public QAbstractModel(Path<? extends AbstractModel> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAbstractModel(PathMetadata<?> metadata) {
        super(AbstractModel.class, metadata);
    }

}

