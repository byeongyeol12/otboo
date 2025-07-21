package com.codeit.otboo.domain.feed.service.impl;

import static com.codeit.otboo.global.error.ErrorCode.*;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.feed.entity.FeedLike;
import com.codeit.otboo.domain.feed.repository.FeedLikeRepository;
import com.codeit.otboo.domain.feed.repository.FeedRepository;
import com.codeit.otboo.domain.feed.service.FeedLikeService;
import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.entity.NotificationLevel;
import com.codeit.otboo.domain.notification.service.NotificationService;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedLikeServiceImpl implements FeedLikeService {

	private final UserRepository userRepository;
	private final FeedLikeRepository feedLikeRepository;
	private final FeedRepository feedRepository;
	private final NotificationService notificationService;

	@Transactional
	@Override
	public void likeFeed(UUID myUSerId, UUID feedId) {
		User user = userRepository.findById(myUSerId)
			.orElseThrow(() -> new CustomException(USER_NOT_FOUND)); //좋아요 찍은 사람

		Feed feed = feedRepository.findById(feedId)
			.orElseThrow(() -> new CustomException(FEED_NOT_FOUND));

		if (feedLikeRepository.existsByFeedAndUser(feed, user)) {
			throw new CustomException(FEED_LIKE_ALREADY); //피드 좋아요 레포 : 피드와 좋아요를 찍은 사람레포. 이미 있으면 말이 안되는 상황
		}

		FeedLike feedLike = new FeedLike(feed, user);
		feedLikeRepository.save(feedLike);
		feed.like();
		if (feed.getUser() == user) feed.likedByMe();

		// 내 피드에 좋아요 시 알림 발생
		log.info("피드에 좋아요 알림");
		notificationService.createAndSend(
			new NotificationDto(
				UUID.randomUUID(),
				Instant.now(),
				myUSerId,
				"Feed_Like",
				"["+user.getName()+"] 님이 피드 에 좋아요를 눌렀습니다.",
				NotificationLevel.INFO
			)
		);
	}

	@Transactional
	@Override
	public void unlikeFeed(UUID myUserId, UUID feedId) {
		User user = userRepository.findById(myUserId)
			.orElseThrow(() -> new CustomException(USER_NOT_FOUND));

		Feed feed = feedRepository.findById(feedId)
			.orElseThrow(() -> new CustomException(FEED_NOT_FOUND));

		FeedLike feedLike = feedLikeRepository.findByFeedAndUser(feed, user)
			.orElseThrow(() -> new CustomException(FEED_LIKE_NOT_FOUND));

		feedLikeRepository.delete(feedLike);
		feed.unlike();
		if (feed.getUser() == user) feed.unlikedByMe();


	}
}
