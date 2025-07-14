package com.codeit.otboo.domain.feed.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.codeit.otboo.domain.feed.entity.FeedComment;

public interface FeedCommentRepositoryCustom {
	List<FeedComment> findByFeedIdCursor(
		UUID feedId,
		Instant cursor,
		UUID idAfter,
		int limit
	);
}
