package com.codeit.otboo.domain.user.repository;

import java.util.List;
import java.util.UUID;

import com.codeit.otboo.domain.user.dto.request.UserSearchRequest;
import com.codeit.otboo.domain.user.entity.QUser;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.global.enumType.Role;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserSearchRepositoryImpl implements UserSearchRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<User> search(UserSearchRequest request) {
		QUser user = QUser.user;

		int limit = (request.limit() != null) ? request.limit() : 20; // 기본값 20

		return queryFactory
			.select(user)
			.from(user)
			.where(
				emailLike(request.emailLike()),
				roleEq(request.roleEqual()),
				lockedEq(request.locked()),
				idAfter(request.idAfter())
			)
			.orderBy(getSort(request.sortBy(), request.sortDirection()))
			.limit(limit)
			.fetch();
	}

	@Override
	public long count(UserSearchRequest request) {
		QUser user = QUser.user;

		return queryFactory
			.select(user.count())
			.from(user)
			.where(
				emailLike(request.emailLike()),
				roleEq(request.roleEqual()),
				lockedEq(request.locked())
			)
			.fetchOne();
	}

	//필터 조건
	private BooleanExpression emailLike(String emailLike) {
		return (emailLike == null) ? null : QUser.user.email.containsIgnoreCase(emailLike);
	}

	private BooleanExpression roleEq(Role role) {
		return (role == null) ? null : QUser.user.role.eq(role);
	}

	private BooleanExpression lockedEq(Boolean locked) {
		return (locked == null) ? null : QUser.user.locked.eq(locked);
	}

	private BooleanExpression idAfter(UUID idAfter) {
		return (idAfter == null) ? null : QUser.user.id.gt(idAfter);
	}

	// 정렬 조건
	private OrderSpecifier<?> getSort(String sortBy, String direction) {
		QUser user = QUser.user;

		// 기본값 설정
		if (sortBy == null || sortBy.isBlank()) {
			sortBy = "createdAt"; // or "email", 요구사항에 맞게
		}
		if (direction == null || direction.isBlank()) {
			direction = "DESC"; // or ASC
		}

		Order order = direction.equalsIgnoreCase("DESC") ? Order.DESC : Order.ASC;

		return switch (sortBy) {
			case "email" -> new OrderSpecifier<>(order, user.email);
			case "name" -> new OrderSpecifier<>(order, user.name);
			case "createdAt" -> new OrderSpecifier<>(order, user.createdAt);
			case "role" -> new OrderSpecifier<>(order, user.role);
			default -> throw new IllegalArgumentException("지원하지 않는 정렬 필드입니다: " + sortBy);
		};
	}
}
