package com.codeit.otboo.domain.feed.dto.response;

import com.codeit.otboo.domain.clothes.dto.response.ClothesAttributeWithDefDto;
import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.feed.entity.Ootd;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.weather.dto.WeatherForFeedDto;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.user.dto.response.AuthorDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FeedDto(
	UUID id,
	Instant createdAt,
	Instant updatedAt,
	AuthorDto author,
	WeatherForFeedDto weather,
	List<OotdDto> ootds,
	String content,
	long likeCount,
	int commentCount,
	boolean likedByMe
) {

	public static FeedDto fromEntity(Feed feed, boolean likedByMe) {
		// Author
		User authorEntity = feed.getUser();
		AuthorDto authorDto = new AuthorDto(
			authorEntity.getId(),
			authorEntity.getName(),
			authorEntity.getProfile() != null ? authorEntity.getProfile().getProfileImageUrl() : null
		);

		// Weather
		Weather weatherEntity = feed.getWeather();
		WeatherForFeedDto weatherDto = new WeatherForFeedDto(
			weatherEntity.getId(),
			weatherEntity.getSkyStatus(),
			weatherEntity.getPrecipitation(),
			weatherEntity.getTemperature()
		);

		// OOTDs
		List<OotdDto> ootdList = feed.getClothesFeeds().stream()
			.map(Ootd::getClothes)
			.map(c -> new OotdDto(
				c.getId(),
				c.getName(),
				c.getImageUrl(),
				c.getType().name(),
				c.getAttributes().stream()
					.map(a -> new ClothesAttributeWithDefDto(
						a.getAttributeDef().getId(),
						a.getAttributeDef().getName(),
						a.getAttributeDef().getSelectableValues(),
						a.getValue()
					))
					.toList()
			))
			.toList();
		//check for this feed likedByMe

		return new FeedDto(
			feed.getId(),
			feed.getCreatedAt(),
			feed.getUpdatedAt(),
			authorDto,
			weatherDto,
			ootdList,
			feed.getContent(),
			feed.getLikeCount(),
			feed.getCommentCount(),
			likedByMe
		);
	}

	public FeedDto withLikedByMe(boolean likedByMe) {
		return new FeedDto(
			this.id,
			this.createdAt,
			this.updatedAt,
			this.author,
			this.weather,
			this.ootds,
			this.content,
			this.likeCount,
			this.commentCount,
			likedByMe
		);
	}
}

