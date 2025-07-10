package com.codeit.otboo.domain.feed.service;

import java.util.UUID;

public interface FeedLikeService {

	void likeFeed(UUID myUserId, UUID feedId);

	void unlikeFeed(UUID myUserId, UUID feedId);

}