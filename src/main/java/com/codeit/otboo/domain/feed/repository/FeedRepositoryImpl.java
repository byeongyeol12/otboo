// package com.codeit.otboo.domain.feed.repository;
//
// import java.time.Instant;
// import java.util.List;
// import java.util.UUID;
//
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageImpl;
// import org.springframework.data.domain.Pageable;
// import org.springframework.stereotype.Repository;
//
// import com.codeit.otboo.domain.feed.dto.request.FeedSearchRequest;
// import com.codeit.otboo.domain.feed.dto.response.FeedPageResponse;
// import com.codeit.otboo.domain.feed.entity.Feed;
// import com.codeit.otboo.domain.feed.entity.QFeed;
// import com.codeit.otboo.domain.user.entity.QUser;
// import com.codeit.otboo.domain.weather.entity.vo.PrecipitationType;
// import com.codeit.otboo.domain.weather.entity.vo.SkyStatus;
// import com.querydsl.core.types.Order;
// import com.querydsl.core.types.OrderSpecifier;
// import com.querydsl.core.types.dsl.BooleanExpression;
// import com.querydsl.jpa.impl.JPAQueryFactory;
//
// import lombok.RequiredArgsConstructor;
//
// @Repository
// @RequiredArgsConstructor
// public class FeedRepositoryImpl implements FeedRepositoryCustom {
//
// 	private final JPAQueryFactory jpaQueryFactory;
//
// 	@Override
// 	public Page<Feed> findFeeds(Pageable pageable, FeedSearchRequest request, UUID currentUserId) {
// 		QFeed feed = QFeed.feed;
// 		QUser user = QUser.user;
//
// 		// 동적 필터
// 		BooleanExpression weatherEq       = weatherEq(request.getSkyStatusEqual());
// 		BooleanExpression precipitationEq = precipitationEq(request.getPrecipitationTypeEqual());
// 		BooleanExpression skyStatusEq     = skyStatusEq(request.getSkyStatusEqual());
// 		BooleanExpression keywordLike     = keywordLike(request.getKeywordLike());
//
// 		// 커서 기반 페이지네이션 조건
// 		BooleanExpression cursorPredicate = buildCursorPredicate(request, feed);
//
// 		// 정렬: primary sort + id tiebreaker
// 		OrderSpecifier<?> primaryOrder   = getSort(request.getSortBy(), request.getSortDirection());
// 		OrderSpecifier<UUID> secondaryId = new OrderSpecifier<>(
// 			"DESCENDING".equalsIgnoreCase(request.getSortDirection()) ? Order.DESC : Order.ASC,
// 			feed.id
// 		);
//
// 		// 데이터 조회: 커서 기반 -> offset/limit 없이 limit만 사용
// 		List<Feed> feeds = jpaQueryFactory
// 			.selectFrom(feed)
// 			.leftJoin(feed.user, user).fetchJoin()
// 			.leftJoin(feed.weather, feed.weather).fetchJoin()
// 			.where(weatherEq, precipitationEq, skyStatusEq, keywordLike, cursorPredicate)
// 			.orderBy(primaryOrder, secondaryId)
// 			.limit(pageable.getPageSize())
// 			.fetch();
//
// 		// 전체 개수
// 		long total = jpaQueryFactory
// 			.select(feed.count())
// 			.from(feed)
// 			.where(weatherEq, precipitationEq, skyStatusEq, keywordLike)
// 			.fetchOne();
//
// 		return new PageImpl<>(feeds, pageable, total);
// 	}
//
// 	private BooleanExpression buildCursorPredicate(FeedSearchRequest request, QFeed feed) {
// 		if (request.getNextCursor() == null || request.getNextIdAfter() == null) {
// 			return null;
// 		}
// 		String sortBy = request.getSortBy();
// 		String direction = request.getSortDirection();
// 		boolean desc = "DESCENDING".equalsIgnoreCase(direction);
//
// 		if ("createdAt".equals(sortBy)) {
// 			Instant cursor = request.getNextCursor();
// 			return desc
// 				? feed.createdAt.before(cursor)
// 				.or(feed.createdAt.eq(cursor).and(feed.id.lt(request.getNextIdAfter())))
// 				: feed.createdAt.after(cursor)
// 				.or(feed.createdAt.eq(cursor).and(feed.id.gt(request.getNextIdAfter())));
// 		} else if ("likeCount".equals(sortBy)) {
// 			long cursor = request.getNextCursorLong();  // FeedSearchRequest에서 파싱된 likeCount
// 			return desc
// 				? feed.likeCount.lt(cursor)
// 				.or(feed.likeCount.eq(cursor).and(feed.id.lt(request.getNextIdAfter())))
// 				: feed.likeCount.gt(cursor)
// 				.or(feed.likeCount.eq(cursor).and(feed.id.gt(request.getNextIdAfter())));
// 		}
// 		return null;
// 	}
//
// 	private BooleanExpression precipitationEq(PrecipitationType type) {
// 		return type == null ? null : QFeed.feed.weather.precipitationType.eq(type);
// 	}
//
// 	private BooleanExpression skyStatusEq(SkyStatus status) {
// 		return status == null ? null : QFeed.feed.weather.skyStatus.eq(status);
// 	}
//
// 	private BooleanExpression keywordLike(String keyword) {
// 		return (keyword == null || keyword.isBlank())
// 			? null
// 			: QFeed.feed.content.containsIgnoreCase(keyword);
// 	}
//
// 	private OrderSpecifier<?> getSort(String sortBy, String direction) {
// 		Order order = "DESCENDING".equalsIgnoreCase(direction) ? Order.DESC : Order.ASC;
// 		switch (sortBy) {
// 			case "likeCount":
// 				return new OrderSpecifier<>(order, QFeed.feed.likeCount);
// 			case "createdAt":
// 			default:
// 				return new OrderSpecifier<>(order, QFeed.feed.createdAt);
// 		}
// 	}
// }