package com.codeit.otboo.domain.clothes.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;

import com.codeit.otboo.domain.clothes.entity.AttributeDef;
import com.codeit.otboo.domain.clothes.entity.QAttributeDef;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AttributeDefRepositoryImpl implements AttributeDefRepositoryCustom {
	private final JPAQueryFactory queryFactory;

	@Override
	public List<AttributeDef> findPagedAttributeDefs(UUID idAfter, int limit, String sortBy, Sort.Direction direction,
		String keywordLike) {
		QAttributeDef qDef = QAttributeDef.attributeDef;

		var query = queryFactory.selectFrom(qDef)
			.where(
				idAfter != null ? qDef.id.gt(idAfter) : null,
				keywordLike != null && !keywordLike.isBlank() ? qDef.name.containsIgnoreCase(keywordLike) : null
			)
			.orderBy(
				direction.isAscending()
					? qDef.name.asc()
					: qDef.name.desc()
			)
			.limit(limit);

		return query.fetch();
	}

}
