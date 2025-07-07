package com.codeit.otboo.domain.clothes.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QClothesAttribute is a Querydsl query type for ClothesAttribute
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QClothesAttribute extends EntityPathBase<ClothesAttribute> {

    private static final long serialVersionUID = 1791958249L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QClothesAttribute clothesAttribute = new QClothesAttribute("clothesAttribute");

    public final QAttributeDef attributeDef;

    public final QClothes clothes;

    public final DateTimePath<java.time.Instant> createdAt = createDateTime("createdAt", java.time.Instant.class);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final DateTimePath<java.time.Instant> updatedAt = createDateTime("updatedAt", java.time.Instant.class);

    public final StringPath value = createString("value");

    public QClothesAttribute(String variable) {
        this(ClothesAttribute.class, forVariable(variable), INITS);
    }

    public QClothesAttribute(Path<? extends ClothesAttribute> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QClothesAttribute(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QClothesAttribute(PathMetadata metadata, PathInits inits) {
        this(ClothesAttribute.class, metadata, inits);
    }

    public QClothesAttribute(Class<? extends ClothesAttribute> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.attributeDef = inits.isInitialized("attributeDef") ? new QAttributeDef(forProperty("attributeDef")) : null;
        this.clothes = inits.isInitialized("clothes") ? new QClothes(forProperty("clothes")) : null;
    }

}

