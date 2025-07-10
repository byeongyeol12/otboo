package com.codeit.otboo.domain.feed.service;
import com.codeit.otboo.domain.feed.dto.response.FeedDto;
import com.codeit.otboo.domain.feed.dto.request.FeedCreateRequest;
import com.codeit.otboo.domain.feed.dto.request.FeedUpdateRequest;
import com.codeit.otboo.domain.feed.dto.response.FeedDtoCursorResponse;
import com.codeit.otboo.domain.weather.entity.vo.PrecipitationType;
import com.codeit.otboo.domain.weather.entity.vo.SkyStatus;

import java.time.Instant;
import java.util.UUID;

public interface FeedService {

	FeedDto createFeed(FeedCreateRequest request);

	FeedDto getFeed(UUID feedId);


	FeedDto updateFeed(UUID feedId, FeedUpdateRequest request);

	void deleteFeed(UUID feedId);

	FeedDtoCursorResponse listByCursor(Instant cursorCreatedAt, Long cursorLikeCount, UUID idAfter, int limit, String sortBy, String sortDirection, String keywordLike, SkyStatus skyStatusEqual, PrecipitationType precipitationTypeEqual, UUID authorIdEqual);

}