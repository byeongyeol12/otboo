package com.codeit.otboo.domain.feed.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.codeit.otboo.domain.feed.entity.FeedComment;

public interface FeedCommentRepository extends JpaRepository<FeedComment, UUID> {

	@Query("""
      SELECT c
      FROM FeedComment c
      JOIN FETCH c.author u
      WHERE c.feed.id = :feedId
        AND ( :cursor IS NULL
              OR (c.createdAt < :cursor
                  OR (c.createdAt = :cursor AND c.id < :idAfter))
            )
      ORDER BY c.createdAt DESC, c.id DESC
      """)
	List<FeedComment> findCommentsByCreatedAtCursor(
		@Param("feedId") UUID feedId,
		@Param("cursor") Instant cursor,
		@Param("idAfter") UUID idAfter,
		Pageable pageable
	);

	@Query("""
      SELECT COUNT(c)
      FROM FeedComment c
      WHERE c.feed.id = :feedId
      """)
	long countByFeedId(
		@Param("feedId") UUID feedId
	);
}
