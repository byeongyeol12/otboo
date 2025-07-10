package com.codeit.otboo.domain.feed.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.feed.entity.FeedLike;
import com.codeit.otboo.domain.user.entity.User;

public interface FeedLikeRepository extends JpaRepository<FeedLike, UUID> {
	boolean existsByFeedAndUser(Feed feed, User user);
	List<FeedLike> findAllByUserIdAndFeedIdIn(UUID userId, List<UUID> feedIds);
	List<FeedLike> findAllByFeedId(UUID feedId);
	Optional<FeedLike> findByFeedAndUser(Feed feed, User user);
}
