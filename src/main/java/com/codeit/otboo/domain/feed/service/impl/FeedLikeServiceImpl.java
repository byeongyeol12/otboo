package com.codeit.otboo.domain.feed.service.impl;

import static com.codeit.otboo.global.error.ErrorCode.*;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.feed.entity.FeedLike;
import com.codeit.otboo.domain.feed.repository.FeedLikeRepository;
import com.codeit.otboo.domain.feed.repository.FeedRepository;
import com.codeit.otboo.domain.feed.service.FeedLikeService;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeedLikeServiceImpl implements FeedLikeService {

	private final UserRepository userRepository;
	private final FeedLikeRepository feedLikeRepository;
	private final FeedRepository feedRepository;

	@Transactional
	@Override
	public void likeFeed(UUID myUSerId, UUID feedId) {
		User user = userRepository.findById(myUSerId)
			.orElseThrow(() -> new CustomException(USER_NOT_FOUND));

		Feed feed = feedRepository.findById(feedId)
			.orElseThrow(() -> new CustomException(FEED_NOT_FOUND));

		if (feedLikeRepository.existsByFeedAndUser(feed, user)) {
			throw new CustomException(FEED_LIKE_ALREADY);
		}

		FeedLike feedLike = new FeedLike(feed, user);
		feedLikeRepository.save(feedLike);
		feed.like();
		if (feed.getUser() == user) feed.likedByMe();
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
