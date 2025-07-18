package com.codeit.otboo.domain.feed.service.impl;

import static com.codeit.otboo.global.error.ErrorCode.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.otboo.domain.clothes.entity.Clothes;
import com.codeit.otboo.domain.clothes.repository.ClothesRepository;
import com.codeit.otboo.domain.feed.dto.request.FeedCreateRequest;
import com.codeit.otboo.domain.feed.dto.request.FeedUpdateRequest;
import com.codeit.otboo.domain.feed.dto.response.FeedDto;
import com.codeit.otboo.domain.feed.dto.response.FeedDtoCursorResponse;
import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.feed.repository.FeedLikeRepository;
import com.codeit.otboo.domain.feed.repository.FeedRepository;
import com.codeit.otboo.domain.feed.service.FeedService;
import com.codeit.otboo.domain.follow.entity.Follow;
import com.codeit.otboo.domain.follow.repository.FollowRepository;
import com.codeit.otboo.domain.follow.service.FollowService;
import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.entity.NotificationLevel;
import com.codeit.otboo.domain.notification.service.NotificationService;
import com.codeit.otboo.domain.user.entity.Profile;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.entity.vo.PrecipitationType;
import com.codeit.otboo.domain.weather.entity.vo.SkyStatus;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.config.security.UserPrincipal;
import com.codeit.otboo.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedServiceImpl implements FeedService {

	private final FeedRepository feedRepository;
	private final UserRepository userRepository;
	private final WeatherRepository weatherRepository;
	private final ClothesRepository clothesRepository;
	private final FeedLikeRepository feedLikeRepository;
	private final NotificationService notificationService;
	private final FollowService followService;
	private final FollowRepository followRepository;

	private UUID getCurrentUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			throw new IllegalStateException("인증된 사용자가 없습니다.");
		}
		return ((UserPrincipal) auth.getPrincipal()).getId();
	}

	@Transactional
	@Override
	public FeedDto createFeed(FeedCreateRequest request) {
		// 1) author를 먼저 조회합니다. (폴백 로직에서 사용자 위치가 필요하기 때문)
		User author = userRepository.findById(request.getAuthorId())
				.orElseThrow(() -> new CustomException(USER_NOT_FOUND));

		// 2) 프론트에서 받은 weatherId로 날씨를 찾아봅니다.
		Optional<Weather> optionalWeather = weatherRepository.findById(request.getWeatherId());

		Weather weather;
		if (optionalWeather.isPresent()) {
			// 3-1) DB에 날씨 정보가 있으면, 그걸 사용합니다.
			weather = optionalWeather.get();
		} else {
			// 3-2) DB에 날씨 정보가 없으면 (프론트가 오래된 ID를 보낸 경우),
			//      사용자 위치를 기반으로 최신 날씨를 다시 찾아옵니다.
			Profile profile = Optional.ofNullable(author.getProfile())
					.orElseThrow(() -> new CustomException(PROFILE_NOT_FOUND));

			Integer x = profile.getX();
			Integer y = profile.getY();
			if (x == null || y == null) {
				throw new CustomException(LOCATION_NOT_SET);
			}

			weather = weatherRepository.findLatestWeatherByLocation(x, y)
					.orElseThrow(() -> new CustomException(WEATHER_NOT_FOUND_FOR_LOCATION));
		}

		// 4) Feed 생성 (이제 weather 변수에는 항상 유효한 값이 들어있습니다)
		Feed feed = new Feed(author, weather, false, 0L, 0, request.getContent());

		// 5) 각 clothesId → Ootd 생성 및 Feed에 추가
		for (UUID clothesId : request.getClothesIds()) {
			Clothes clothes = clothesRepository.findById(clothesId)
					.orElseThrow(() -> new CustomException(CLOTHES_NOT_FOUND));
			feed.addClothes(clothes);
		}

		// 6) 저장
		feed = feedRepository.save(feed);

		// 팔로우한 사용자가 피드 등록시 알림 발생(피드 작성한 사람 : 팔로이)
		log.info("팔로우한 사용자가 피드를 등록 알림");
		List<Follow> followers = followRepository.findAllByFolloweeId(author.getId());

		// 팔로워가 있을 때만 알림 전송
		for (Follow follow : followers) {
			UUID followerId = follow.getFollower().getId(); // 팔로워 유저의 ID
			try {
				notificationService.createAndSend(
					new NotificationDto(
						UUID.randomUUID(),
						Instant.now(),
						followerId,
						"Feed_Create",
						"[" + author.getName() + "] 님이 새 피드를 등록했습니다.",
						NotificationLevel.INFO
					)
				);
				log.info("[createFeed] 팔로워 알림 전송 성공: followerId={}", followerId);
			} catch (Exception e) {
				log.error("[createFeed] 팔로워 알림 전송 실패: followerId={}, error={}", followerId, e.getMessage(), e);
			}
		}

		// 7) DTO로 변환
		return FeedDto.fromEntity(feed, false);
	}
	@Transactional(readOnly = true)
	@Override
	public FeedDto getFeed(UUID feedId) {
		Feed feed = feedRepository.findById(feedId)
			.orElseThrow(() -> new CustomException(FEED_NOT_FOUND));
		UUID currentUserId = getCurrentUserId();
		boolean likedByMe = feedLikeRepository
			.existsByUserIdAndFeedId(currentUserId, feedId);

		return FeedDto.fromEntity(feed, likedByMe);
	}

	@Transactional
	@Override
	public FeedDto updateFeed(UUID feedId, FeedUpdateRequest request) {
		Feed feed = feedRepository.findById(feedId)
			.orElseThrow(() -> new CustomException(ErrorCode.FEED_NOT_FOUND));
		feed.updateContent(request.getContent());
		Feed updatedFeed = feedRepository.save(feed);

		UUID currentUserId = getCurrentUserId();
		boolean likedByMe = feedLikeRepository
			.existsByUserIdAndFeedId(currentUserId, feedId);

		return FeedDto.fromEntity(updatedFeed, likedByMe);
	}

	@Transactional
	@Override
	public void deleteFeed(UUID feedId) {
		if(!feedRepository.existsById(feedId)){
			throw new CustomException(ErrorCode.FEED_NOT_FOUND);
		}
		feedRepository.deleteById(feedId);
	}

	@Transactional(readOnly = true)
	@Override
	public FeedDtoCursorResponse listByCursor(
		Instant cursorCreatedAt,
		Long cursorLikeCount,
		UUID idAfter,
		int limit,
		String sortBy,
		String sortDirection,
		String keywordLike,
		SkyStatus skyStatusEqual,
		PrecipitationType precipitationTypeEqual,
		UUID authorIdEqual
	) {
		int fetchSize = limit + 1;
		List<com.codeit.otboo.domain.feed.entity.Feed> all =
			"likeCount".equalsIgnoreCase(sortBy)
				? feedRepository.findByLikeCountCursor(
				keywordLike, skyStatusEqual, precipitationTypeEqual,
				cursorLikeCount, idAfter, fetchSize
			)
				: feedRepository.findByCreatedAtCursor(
				keywordLike, skyStatusEqual, precipitationTypeEqual,
				cursorCreatedAt, idAfter, fetchSize
			);

		boolean hasNext = all.size() > limit;
		List<com.codeit.otboo.domain.feed.entity.Feed> page =
			hasNext ? all.subList(0, limit) : all;

		UUID currentUserId = getCurrentUserId();

		List<UUID> feedIds = page.stream()
			.map(Feed::getId)
			.toList();
		Set<UUID> likedFeedIds = feedLikeRepository
			.findByUserIdAndFeedIdIn(currentUserId, feedIds)
			.stream()
			.map(like -> like.getFeed().getId())
			.collect(Collectors.toSet());

		List<FeedDto> data = page.stream()
			.map(feed ->
					FeedDto.fromEntity(feed, likedFeedIds.contains(feed.getId()))
			)
			.toList();

		Instant nextCursorCreatedAt = null;
		Long    nextCursorLikeCount = null;
		UUID    nextIdAfter          = null;

		if (hasNext) {
			var last = page.get(page.size() - 1);
			nextIdAfter = last.getId();
			if ("likeCount".equalsIgnoreCase(sortBy)) {
				nextCursorLikeCount = last.getLikeCount();
			} else {
				nextCursorCreatedAt = last.getCreatedAt();
			}
		}

		long total = feedRepository.countByFilters(
			keywordLike, skyStatusEqual, precipitationTypeEqual
		);

		return new FeedDtoCursorResponse(
			data,
			"likeCount".equalsIgnoreCase(sortBy)
				? nextCursorLikeCount != null ? nextCursorLikeCount.toString() : null
				: nextCursorCreatedAt != null    ? nextCursorCreatedAt.toString() : null,
			nextIdAfter,
			hasNext,
			total,
			sortBy,
			sortDirection
		);
	}
}
