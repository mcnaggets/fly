package by.fly.model;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = 550665826L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUser user = new QUser("user");

    public final QHuman _super = new QHuman(this);

    //inherited
    public final StringPath address = _super.address;

    public final StringPath barcode = createString("barcode");

    //inherited
    public final StringPath email = _super.email;

    //inherited
    public final StringPath id = _super.id;

    public final StringPath login = createString("login");

    //inherited
    public final StringPath name = _super.name;

    public final QOrganization organization;

    public final StringPath password = createString("password");

    //inherited
    public final StringPath phone = _super.phone;

    public final ListPath<Role, QRole> roles = this.<Role, QRole>createList("roles", Role.class, QRole.class, PathInits.DIRECT2);

    public QUser(String variable) {
        this(User.class, forVariable(variable), INITS);
    }

    public QUser(Path<? extends User> path) {
        this(path.getType(), path.getMetadata(), path.getMetadata().isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QUser(PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QUser(PathMetadata<?> metadata, PathInits inits) {
        this(User.class, metadata, inits);
    }

    public QUser(Class<? extends User> type, PathMetadata<?> metadata, PathInits inits) {
        super(type, metadata, inits);
        this.organization = inits.isInitialized("organization") ? new QOrganization(forProperty("organization")) : null;
    }

}

