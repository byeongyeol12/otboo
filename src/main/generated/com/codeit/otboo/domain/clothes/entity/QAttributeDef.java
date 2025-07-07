package com.codeit.otboo.domain.clothes.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAttributeDef is a Querydsl query type for AttributeDef
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAttributeDef extends EntityPathBase<AttributeDef> {

    private static final long serialVersionUID = -794559490L;

    public static final QAttributeDef attributeDef = new QAttributeDef("attributeDef");

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final StringPath name = createString("name");

    public final ListPath<String, StringPath> selectableValues = this.<String, StringPath>createList("selectableValues", String.class, StringPath.class, PathInits.DIRECT2);

    public QAttributeDef(String variable) {
        super(AttributeDef.class, forVariable(variable));
    }

    public QAttributeDef(Path<? extends AttributeDef> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAttributeDef(PathMetadata metadata) {
        super(AttributeDef.class, metadata);
    }

}

