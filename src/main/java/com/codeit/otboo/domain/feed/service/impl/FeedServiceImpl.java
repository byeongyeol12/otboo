package com.codeit.otboo.domain.feed.service.impl;

import static com.codeit.otboo.global.error.ErrorCode.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.otboo.domain.auth.service.AuthService;
import com.codeit.otboo.domain.clothes.entity.Clothes;
import com.codeit.otboo.domain.clothes.repository.ClothesRepository;
import com.codeit.otboo.domain.feed.dto.request.FeedCreateRequest;
import com.codeit.otboo.domain.feed.dto.request.FeedUpdateRequest;
import com.codeit.otboo.domain.feed.dto.response.FeedDto;
import com.codeit.otboo.domain.feed.dto.response.FeedDtoCursorResponse;
import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.feed.entity.Ootd;
import com.codeit.otboo.domain.feed.repository.FeedRepository;
import com.codeit.otboo.domain.feed.repository.OotdRepository;
import com.codeit.otboo.domain.feed.service.FeedService;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.entity.vo.PrecipitationType;
import com.codeit.otboo.domain.weather.entity.vo.SkyStatus;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

	private final FeedRepository feedRepository;
	private final UserRepository userRepository;
	private final WeatherRepository weatherRepository;
	private final ClothesRepository clothesRepository;

	@Transactional
	public FeedDto createFeed(FeedCreateRequest request) {
		// 1) author, weather 조회
		User author = userRepository.findById(request.getAuthorId())
			.orElseThrow(() -> new CustomException(USER_NOT_FOUND));
		Weather weather = weatherRepository.findById(request.getWeatherId())
			.orElseThrow(() -> new CustomException(WEATHER_NOT_FOUND));

		// 2) Feed 생성
		Feed feed = new Feed(author, weather, false, 0L, 0, request.getContent());

		// 3) 각 clothesId → Ootd 생성 및 Feed에 추가
		for (UUID clothesId : request.getClothesIds()) {
			Clothes clothes = clothesRepository.findById(clothesId)
				.orElseThrow(() -> new CustomException(CLOTHES_NOT_FOUND));
			feed.addClothes(clothes);
		}

		// 4) 저장 (cascade=PERSIST 덕분에 Ootd도 함께 insert)
		feed = feedRepository.save(feed);

		// 5) DTO로 변환
		return FeedDto.fromEntity(feed);
	}

	@Transactional(readOnly = true)
	@Override
	public FeedDto getFeed(UUID feedId) {
		Feed feed = feedRepository.findById(feedId)
			.orElseThrow(() -> new CustomException(FEED_NOT_FOUND));

		return FeedDto.fromEntity(feed);
	}

	@Transactional
	@Override
	public FeedDto updateFeed(UUID feedId, FeedUpdateRequest request) {
		Feed feed = feedRepository.findById(feedId)
			.orElseThrow(() -> new CustomException(ErrorCode.FEED_NOT_FOUND));
		feed.updateContent(request.getContent());
		Feed updatedFeed = feedRepository.save(feed);

		return FeedDto.fromEntity(updatedFeed);
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

		PageRequest pageReq = PageRequest.of(0, limit);
		List<Feed> feeds = "likeCount".equalsIgnoreCase(sortBy)
			? feedRepository.findFeedsByLikeCountCursor(
			keywordLike, skyStatusEqual, precipitationTypeEqual, cursorLikeCount, idAfter, pageReq
		)
			: feedRepository.findFeedsByCreatedAtCursor(
			keywordLike, skyStatusEqual, precipitationTypeEqual, cursorCreatedAt, idAfter, pageReq
		);

		List<FeedDto> data = feeds.stream()
			.map(feed -> FeedDto.fromEntity(feed))
			.toList();

		boolean hasNext = data.size() == limit;
		String nextCursor = null;
		UUID nextIdAfter = null;

		if(hasNext) {
			Feed last = feeds.get(feeds.size() - 1);
			nextIdAfter = last.getId();
			nextCursor = "likeCount".equalsIgnoreCase(sortBy)
				? String.valueOf(last.getLikeCount())
				: last.getCreatedAt().toString();
		}

		long totalCount = feedRepository.countByFilters(keywordLike, skyStatusEqual, precipitationTypeEqual);

		return new FeedDtoCursorResponse(
			data,
			nextCursor,
			nextIdAfter,
			hasNext,
			totalCount,
			sortBy,
			sortDirection
		);
	}
}
