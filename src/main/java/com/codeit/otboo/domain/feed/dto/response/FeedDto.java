package com.codeit.otboo.domain.feed.dto.response;

import com.codeit.otboo.domain.clothes.dto.response.ClothesAttributeWithDefDto;
import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.feed.entity.Ootd;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.weather.dto.WeatherForFeedDto;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.user.dto.response.AuthorDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "피드(게시글) 응답 DTO")
public record FeedDto(
		@Schema(description = "피드 ID", example = "5ef2c1b2-75e6-4d87-9f93-7d0c8de0ba16")
		UUID id,

		@Schema(description = "피드 생성 시각(UTC ISO8601)", example = "2024-07-25T11:40:31.000Z")
		Instant createdAt,

		@Schema(description = "피드 수정 시각(UTC ISO8601)", example = "2024-07-25T12:00:05.000Z")
		Instant updatedAt,

		@Schema(description = "작성자 정보")
		AuthorDto author,

		@Schema(description = "날씨 정보")
		WeatherForFeedDto weather,

		@Schema(description = "OOTD(의상) 리스트")
		List<OotdDto> ootds,

		@Schema(description = "피드 내용", example = "오늘은 맑고 선선해서 셔츠와 청바지를 입었어요!")
		String content,

		@Schema(description = "피드 좋아요 개수", example = "21")
		long likeCount,

		@Schema(description = "피드 댓글 개수", example = "5")
		int commentCount,

		@Schema(description = "내가 이 피드를 좋아요 했는지 여부", example = "false")
		boolean likedByMe
) {
	// ... fromEntity 및 withLikedByMe 메서드는 기존 그대로 사용
}
