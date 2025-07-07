package com.codeit.otboo.domain.feed.service;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.codeit.otboo.domain.clothes.dto.response.ClothesAttributeWithDefDto;
import com.codeit.otboo.domain.clothes.dto.response.ClothesDto;
import com.codeit.otboo.domain.clothes.entity.Clothes;
import com.codeit.otboo.domain.clothes.repository.ClothesAttributeRepository;
import com.codeit.otboo.domain.clothes.repository.ClothesRepository;
import com.codeit.otboo.domain.feed.dto.*;
import com.codeit.otboo.domain.feed.entity.*;
import com.codeit.otboo.domain.feed.repository.*;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.domain.weather.dto.WeatherDto;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeedService {

	private final FeedRepository feedRepository;
	private final OotdRepository ootdRepository;
	private final FeedLikeRepository feedLikeRepository;
	private final FeedCommentRepository feedCommentRepository;
	private final ClothesAttributeRepository clothesAttributeRepository;
	private final ClothesRepository clothesRepository;
	private final UserRepository userRepository;
	private final WeatherRepository weatherRepository;

	@Transactional
	public FeedDto createFeed(UUID currentUserId, FeedCreateRequest request) {
		// Validate user and weather exist
		User user = userRepository.findById(currentUserId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
		Weather weather = weatherRepository.findById(request.weatherId()).orElseThrow(() -> new CustomException(ErrorCode.WEATHER_NOT_FOUND));

		// Validate clothes exist and belong to current user
		List<Clothes> clothesList = new ArrayList<>();
		for (UUID clothesId : request.clothesIds()) {
			Clothes clothes = clothesRepository.findById(clothesId)
				.orElseThrow(() -> new CustomException(ErrorCode.CLOTHES_NOT_FOUND));
			if (!clothes.getOwnerId().equals(currentUserId)) {
				throw new CustomException(ErrorCode.CLOTHES_NOT_FOUND);
			}
			clothesList.add(clothes);
		}

		// Create and save Feed
		Feed feed = Feed.builder()
			.user(user)
			.weather(weather)
			.likedByMe(false)
			.likeCount(0L)
			.commentCount(0)
			.content(request.content())
			.build();
		feed = feedRepository.save(feed);

		// Save outfit (Ootd) entries linking feed and each selected clothes
		for (Clothes clothes : clothesList) {
			Ootd ootd = new Ootd(feed, clothes);
			ootdRepository.save(ootd);
		}

		// Return FeedDto for the newly created feed
		return mapToFeedDto(feed, false);
	}

	@Transactional(readOnly = true)
	public Page<FeedDto> getFeeds(UUID currentUserId, Pageable pageable) {
		Page<Feed> feedPage = feedRepository.findFeeds(pageable, currentUserId);
		List<UUID> feedIds = feedPage.getContent().stream()
			.map(Feed::getId).collect(Collectors.toList());
		Set<UUID> likedFeedIds = new HashSet<>();
		if (!feedIds.isEmpty()) {
			feedLikeRepository.findAllByUserIdAndFeedIdIn(currentUserId, feedIds)
				.forEach(like -> likedFeedIds.add(like.getFeed().getId()));
		}
		List<FeedDto> feedDtos = new ArrayList<>();
		for (Feed feed : feedPage.getContent()) {
			boolean likedByMe = likedFeedIds.contains(feed.getId());
			feedDtos.add(mapToFeedDto(feed, likedByMe));
		}
		return new PageImpl<>(feedDtos, pageable, feedPage.getTotalElements());
	}

	@Transactional
	public FeedDto updateFeed(UUID currentUserId, UUID feedId, FeedUpdateRequest request) {
		Feed feed = feedRepository.findById(feedId)
			.orElseThrow(() -> new CustomException(ErrorCode.FEED_NOT_FOUND));
		if (!feed.getUser().getId().equals(currentUserId)) {
			throw new CustomException(ErrorCode.FEED_NOT_AUTHOR);
		}
		// Update fields if provided
		if (request.weatherId() != null) {
			Weather weather = weatherRepository.findById(request.weatherId())
				.orElseThrow(() -> new CustomException(ErrorCode.WEATHER_NOT_FOUND));
			feed.setWeather(weather);
		}
		if (request.content() != null) {
			feed.setContent(request.content());
		}
		if (request.clothesIds() != null) {
			// Validate and update outfit clothes
			// Remove old outfit links
			ootdRepository.findByFeedIdIn(Collections.singletonList(feed.getId()))
				.forEach(ootd -> ootdRepository.delete(ootd));
			// Add new links
			for (UUID clothesId : request.clothesIds()) {
				Clothes clothes = clothesRepository.findById(clothesId)
					.orElseThrow(() -> new CustomException(ErrorCode.CLOTHES_NOT_FOUND));
				if (!clothes.getOwnerId().equals(currentUserId)) {
					throw new CustomException(ErrorCode.CLOTHES_NOT_FOUND);
				}
				Ootd ootd = new Ootd(feed, clothes);
				ootdRepository.save(ootd);
			}
		}
		// likeCount and commentCount remain unchanged
		Feed updatedFeed = feedRepository.save(feed);
		boolean likedByMe = !feedLikeRepository.findAllByUserIdAndFeedIdIn(currentUserId, Collections.singletonList(feedId)).isEmpty();
		return mapToFeedDto(updatedFeed, likedByMe);
	}

	@Transactional
	public void deleteFeed(UUID currentUserId, UUID feedId) {
		Feed feed = feedRepository.findById(feedId)
			.orElseThrow(() -> new CustomException(ErrorCode.FEED_NOT_FOUND));
		if (!feed.getUser().getId().equals(currentUserId)) {
			throw new CustomException(ErrorCode.FEED_NOT_AUTHOR);
		}
		// Delete related entities first (to satisfy foreign key constraints)
		feedCommentRepository.findByFeedIdOrderByCreatedAtAsc(feedId)
			.forEach(comment -> feedCommentRepository.delete(comment));
		feedLikeRepository.findAllByFeedId(feedId)
			.forEach(like -> feedLikeRepository.delete(like));
		ootdRepository.findByFeedIdIn(Arrays.asList(feedId))
			.forEach(ootd -> ootdRepository.delete(ootd));
		feedRepository.delete(feed);
	}

	@Transactional
	public void likeFeed(UUID currentUserId, UUID feedId) {
		Feed feed = feedRepository.findById(feedId)
			.orElseThrow(() -> new CustomException(ErrorCode.FEED_NOT_FOUND));
		User user = userRepository.findById(currentUserId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
		if (feedLikeRepository.existsByFeedAndUser(feed, user)) {
			throw new CustomException(ErrorCode.FEED_ALREADY_LIKED);
		}
		// Save like record
		FeedLike like = new FeedLike(feed, user);
		feedLikeRepository.save(like);
		// Increment like count
		feed.setLikeCount(feed.getLikeCount() + 1);
		feedRepository.save(feed);
	}

	@Transactional
	public void unlikeFeed(UUID currentUserId, UUID feedId) {
		Feed feed = feedRepository.findById(feedId)
			.orElseThrow(() -> new CustomException(ErrorCode.FEED_NOT_FOUND));
		User user = userRepository.findById(currentUserId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
		// Find existing like
		List<FeedLike> likes = feedLikeRepository.findAllByUserIdAndFeedIdIn(currentUserId, Collections.singletonList(feedId));
		if (likes.isEmpty()) {
			throw new CustomException(ErrorCode.FEED_LIKE_NOT_FOUND);
		}
		FeedLike like = likes.get(0);
		feedLikeRepository.delete(like);
		// Decrement like count
		feed.setLikeCount(Math.max(0, feed.getLikeCount() - 1));
		feedRepository.save(feed);
	}

	@Transactional(readOnly = true)
	public List<FeedCommentDto> getComments(UUID feedId) {
		// Ensure feed exists
		feedRepository.findById(feedId).orElseThrow(() -> new CustomException(ErrorCode.FEED_NOT_FOUND));
		List<FeedComment> comments = feedCommentRepository.findByFeedIdOrderByCreatedAtAsc(feedId);
		List<FeedCommentDto> result = new ArrayList<>();
		for (FeedComment comment : comments) {
			result.add(new FeedCommentDto(
				comment.getId(),
				comment.getAuthor().getId(),
				comment.getAuthor().getName(),
				comment.getContent(),
				comment.getCreatedAt()
			));
		}
		return result;
	}

	@Transactional
	public FeedCommentDto addComment(UUID currentUserId, UUID feedId, FeedCommentRequest request) {
		Feed feed = feedRepository.findById(feedId)
			.orElseThrow(() -> new CustomException(ErrorCode.FEED_NOT_FOUND));
		User user = userRepository.findById(currentUserId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
		FeedComment comment = new FeedComment(feed, user, request.content());
		feedCommentRepository.save(comment);
		// Increment comment count
		feed.setCommentCount(feed.getCommentCount() + 1);
		feedRepository.save(feed);
		return new FeedCommentDto(
			comment.getId(),
			user.getId(),
			user.getName(),
			comment.getContent(),
			comment.getCreatedAt()
		);
	}

	private FeedDto mapToFeedDto(Feed feed, boolean likedByMe) {
		// Fetch clothes linked to this feed
		List<Ootd> ootdList = ootdRepository.findByFeedIdIn(Collections.singletonList(feed.getId()));
		List<Clothes> clothesList = ootdList.stream()
			.map(Ootd::getClothes)
			.collect(Collectors.toList());
		// Fetch attributes for all clothes in this feed
		List<UUID> clothesIds = clothesList.stream().map(Clothes::getId).collect(Collectors.toList());
		Map<UUID, List<ClothesAttributeWithDefDto>> clothesAttrDtoMap = new HashMap<>();
		if (!clothesIds.isEmpty()) {
			List<ClothesAttribute> attrEntities = clothesAttributeRepository.findByClothesIdIn(clothesIds);
			for (ClothesAttribute attr : attrEntities) {
				// Load selectable values list from definition
				attr.getAttributeDef().getSelectableValues().size();
				ClothesAttributeWithDefDto attrDto = new ClothesAttributeWithDefDto(
					attr.getAttributeDef().getId(),
					attr.getAttributeDef().getName(),
					attr.getAttributeDef().getSelectableValues(),
					attr.getValue()
				);
				clothesAttrDtoMap.computeIfAbsent(attr.getClothes().getId(), k -> new ArrayList<>()).add(attrDto);
			}
		}
		// Build ClothesDto list with attributes
		List<ClothesDto> clothesDtos = new ArrayList<>();
		for (Clothes clothes : clothesList) {
			List<ClothesAttributeWithDefDto> attributes = clothesAttrDtoMap.getOrDefault(clothes.getId(), Collections.emptyList());
			clothesDtos.add(new ClothesDto(
				clothes.getId(),
				clothes.getOwnerId(),
				clothes.getName(),
				clothes.getImageUrl(),
				clothes.getType().name(),
				attributes
			));
		}
		// Create WeatherDto from Weather entity
		WeatherDto weatherDto = new WeatherDto(
			feed.getWeather().getId(),
			feed.getWeather().getForecastedAt(),
			feed.getWeather().getForecastAt(),
			feed.getWeather().getLocation(),
			feed.getWeather().getSkyStatus(),
			feed.getWeather().getPrecipitation(),
			feed.getWeather().getHumidity(),
			feed.getWeather().getTemperature(),
			feed.getWeather().getWindSpeed()
		);
		// Assemble and return the FeedDto
		return new FeedDto(
			feed.getId(),
			feed.getUser().getId(),
			feed.getUser().getName(),
			weatherDto,
			likedByMe,
			feed.getLikeCount(),
			feed.getCommentCount(),
			feed.getContent(),
			feed.getCreatedAt(),
			clothesDtos
		);
	}
}
