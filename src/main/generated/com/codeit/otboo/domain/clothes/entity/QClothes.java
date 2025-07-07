package com.codeit.otboo.domain.clothes.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QClothes is a Querydsl query type for Clothes
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QClothes extends EntityPathBase<Clothes> {

    private static final long serialVersionUID = 1428737779L;

    public static final QClothes clothes = new QClothes("clothes");

    public final ListPath<ClothesAttribute, QClothesAttribute> attributes = this.<ClothesAttribute, QClothesAttribute>createList("attributes", ClothesAttribute.class, QClothesAttribute.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.Instant> createdAt = createDateTime("createdAt", java.time.Instant.class);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final StringPath name = createString("name");

    public final ComparablePath<java.util.UUID> ownerId = createComparable("ownerId", java.util.UUID.class);

    public final EnumPath<ClothesType> type = createEnum("type", ClothesType.class);

    public final DateTimePath<java.time.Instant> updatedAt = createDateTime("updatedAt", java.time.Instant.class);

    public QClothes(String variable) {
        super(Clothes.class, forVariable(variable));
    }

    public QClothes(Path<? extends Clothes> path) {
        super(path.getType(), path.getMetadata());
    }

    public QClothes(PathMetadata metadata) {
        super(Clothes.class, metadata);
    }

}

