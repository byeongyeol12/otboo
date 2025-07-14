package com.codeit.otboo.domain.feed.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.weather.entity.vo.PrecipitationType;
import com.codeit.otboo.domain.weather.entity.vo.SkyStatus;

public interface FeedRepositoryCustom {
	List<Feed> findByCreatedAtCursor(
		String keyword,
		SkyStatus skyStatus,
		PrecipitationType precipitationType,
		Instant cursorCreatedAt,
		UUID cursorId,
		int limit
	);

	List<Feed> findByLikeCountCursor(
		String keyword,
		SkyStatus skyStatus,
		PrecipitationType precipitationType,
		Long cursorLikeCount,
		UUID cursorId,
		int limit
	);

	long countByFilters(
		String keyword,
		SkyStatus skyStatus,
		PrecipitationType precipitationType
	);
}
