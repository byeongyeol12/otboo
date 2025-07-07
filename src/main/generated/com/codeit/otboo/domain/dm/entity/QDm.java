package com.codeit.otboo.domain.dm.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDm is a Querydsl query type for Dm
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDm extends EntityPathBase<Dm> {

    private static final long serialVersionUID = 652381919L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDm dm = new QDm("dm");

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.Instant> createdAt = createDateTime("createdAt", java.time.Instant.class);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final com.codeit.otboo.domain.user.entity.QUser receiver;

    public final com.codeit.otboo.domain.user.entity.QUser sender;

    public QDm(String variable) {
        this(Dm.class, forVariable(variable), INITS);
    }

    public QDm(Path<? extends Dm> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDm(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDm(PathMetadata metadata, PathInits inits) {
        this(Dm.class, metadata, inits);
    }

    public QDm(Class<? extends Dm> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.receiver = inits.isInitialized("receiver") ? new com.codeit.otboo.domain.user.entity.QUser(forProperty("receiver"), inits.get("receiver")) : null;
        this.sender = inits.isInitialized("sender") ? new com.codeit.otboo.domain.user.entity.QUser(forProperty("sender"), inits.get("sender")) : null;
    }

}

