package com.codeit.otboo.domain.feed.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.codeit.otboo.domain.feed.entity.FeedComment;

public interface FeedCommentRepository
	extends JpaRepository<FeedComment, UUID>,
	FeedCommentRepositoryCustom {
	long countByFeedId(UUID feedId);
}
