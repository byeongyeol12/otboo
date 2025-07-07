package com.codeit.otboo.domain.clothes.repository;

import java.util.List;
import java.util.UUID;

import com.codeit.otboo.domain.clothes.entity.Clothes;
import com.codeit.otboo.domain.clothes.entity.ClothesType;
import com.codeit.otboo.domain.clothes.entity.QClothes;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClothesRepositoryImpl implements ClothesRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<Clothes> findClothesByOwnerWithCursor(UUID ownerId, UUID idAfter, int limit, ClothesType type) {
		QClothes clothes = QClothes.clothes;

		return queryFactory
			.selectFrom(clothes)
			.where(
				clothes.ownerId.eq(ownerId),

				idAfterCondition(idAfter),

				typeEqualCondition(type)
			)
			.orderBy(clothes.id.asc())
			.limit(limit)
			.fetch();
	}

	private BooleanExpression idAfterCondition(UUID idAfter) {
		QClothes clothes = QClothes.clothes;
		return idAfter != null ? clothes.id.gt(idAfter) : null;
	}

	private BooleanExpression typeEqualCondition(ClothesType type) {
		QClothes clothes = QClothes.clothes;
		return type != null ? clothes.type.eq(type) : null;
	}

}
