package com.codeit.otboo.domain.feed.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import com.codeit.otboo.domain.clothes.dto.response.ClothesDto;
import com.codeit.otboo.domain.weather.dto.WeatherDto;

public record FeedDto(
	UUID id,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt,
	AuthorDto author,
	WeatherDto weather,
	List<OotdDto> ootds,
	String content,
	long likeCount,
	int commentCount,
	boolean likedByMe
) {
}
