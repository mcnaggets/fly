package by.fly.model;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QSettings is a Querydsl query type for Settings
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QSettings extends EntityPathBase<Settings> {

    private static final long serialVersionUID = -1034605766L;

    public static final QSettings settings = new QSettings("settings");

    public final StringPath name = createString("name");

    public final SimplePath<Object> userData = createSimple("userData", Object.class);

    public final SimplePath<Object> value = createSimple("value", Object.class);

    public QSettings(String variable) {
        super(Settings.class, forVariable(variable));
    }

    public QSettings(Path<? extends Settings> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSettings(PathMetadata<?> metadata) {
        super(Settings.class, metadata);
    }

}

