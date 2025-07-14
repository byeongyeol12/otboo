package com.codeit.otboo.domain.feed.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.feed.entity.QFeed;
import com.codeit.otboo.domain.user.entity.QUser;
import com.codeit.otboo.domain.weather.entity.QWeather;
import com.codeit.otboo.domain.weather.entity.vo.PrecipitationType;
import com.codeit.otboo.domain.weather.entity.vo.SkyStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

@Repository
public class FeedRepositoryImpl implements FeedRepositoryCustom {

	private final JPAQueryFactory query;
	private final QFeed feed    = QFeed.feed;
	private final QUser user    = QUser.user;
	private final QWeather weather = QWeather.weather;

	public FeedRepositoryImpl(EntityManager em) {
		this.query = new JPAQueryFactory(em);
	}

	@Override
	public List<Feed> findByCreatedAtCursor(
		String keyword,
		SkyStatus skyStatus,
		PrecipitationType precipitationType,
		Instant cursorCreatedAt,
		UUID cursorId,
		int limit
	) {
		BooleanBuilder where = new BooleanBuilder();

		if (keyword != null && !keyword.isBlank()) {
			where.and(feed.content.containsIgnoreCase(keyword));
		}
		if (skyStatus != null) {
			where.and(feed.weather.skyStatus.eq(skyStatus));
		}
		if (precipitationType != null) {
			where.and(feed.weather.precipitationType.eq(precipitationType));
		}
		if (cursorCreatedAt != null && cursorId != null) {
			where.and(
				feed.createdAt.lt(cursorCreatedAt)
					.or(feed.createdAt.eq(cursorCreatedAt)
						.and(feed.id.lt(cursorId)))
			);
		}

		return query
			.selectFrom(feed)
			.join(feed.user, user).fetchJoin()
			.join(feed.weather, weather).fetchJoin()
			.where(where)
			.orderBy(
				new OrderSpecifier<>(Order.DESC, feed.createdAt),
				new OrderSpecifier<>(Order.DESC, feed.id)
			)
			.limit(limit)
			.fetch();
	}

	@Override
	public List<Feed> findByLikeCountCursor(
		String keyword,
		SkyStatus skyStatus,
		PrecipitationType precipitationType,
		Long cursorLikeCount,
		UUID cursorId,
		int limit
	) {
		BooleanBuilder where = new BooleanBuilder();

		if (keyword != null && !keyword.isBlank()) {
			where.and(feed.content.containsIgnoreCase(keyword));
		}
		if (skyStatus != null) {
			where.and(feed.weather.skyStatus.eq(skyStatus));
		}
		if (precipitationType != null) {
			where.and(feed.weather.precipitationType.eq(precipitationType));
		}
		if (cursorLikeCount != null && cursorId != null) {
			where.and(
				feed.likeCount.lt(cursorLikeCount)
					.or(feed.likeCount.eq(cursorLikeCount)
						.and(feed.id.lt(cursorId)))
			);
		}

		return query
			.selectFrom(feed)
			.join(feed.user, user).fetchJoin()
			.join(feed.weather, weather).fetchJoin()
			.where(where)
			.orderBy(
				new OrderSpecifier<>(Order.DESC, feed.likeCount),
				new OrderSpecifier<>(Order.DESC, feed.id)
			)
			.limit(limit)
			.fetch();
	}

	@Override
	public long countByFilters(
		String keyword,
		SkyStatus skyStatus,
		PrecipitationType precipitationType
	) {
		BooleanBuilder where = new BooleanBuilder();

		if (keyword != null && !keyword.isBlank()) {
			where.and(feed.content.containsIgnoreCase(keyword));
		}
		if (skyStatus != null) {
			where.and(feed.weather.skyStatus.eq(skyStatus));
		}
		if (precipitationType != null) {
			where.and(feed.weather.precipitationType.eq(precipitationType));
		}

		return query
			.select(feed.count())
			.from(feed)
			.join(feed.weather, weather)
			.where(where)
			.fetchOne();
	}
}
