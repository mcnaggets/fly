package by.fly.model;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QOrganization is a Querydsl query type for Organization
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QOrganization extends EntityPathBase<Organization> {

    private static final long serialVersionUID = 395049162L;

    public static final QOrganization organization = new QOrganization("organization");

    public final QAbstractModel _super = new QAbstractModel(this);

    //inherited
    public final StringPath id = _super.id;

    public final StringPath inn = createString("inn");

    public final StringPath name = createString("name");

    public QOrganization(String variable) {
        super(Organization.class, forVariable(variable));
    }

    public QOrganization(Path<? extends Organization> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOrganization(PathMetadata<?> metadata) {
        super(Organization.class, metadata);
    }

}

