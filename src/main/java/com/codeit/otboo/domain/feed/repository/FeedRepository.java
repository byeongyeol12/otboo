package com.codeit.otboo.domain.feed.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.weather.entity.vo.PrecipitationType;
import com.codeit.otboo.domain.weather.entity.vo.SkyStatus;

@Repository
public interface FeedRepository extends JpaRepository<Feed, UUID> {

	/** 생성일순 커서 페이징 + 필터(대소문자 구분 없이 keyword) */
	@Query("""
      SELECT f
      FROM Feed f
      JOIN FETCH f.user u
      JOIN FETCH f.weather w
      WHERE (:keywordLike          IS NULL
             OR LOWER(f.content) LIKE LOWER(CONCAT('%', :keywordLike, '%')))
        AND (:skyStatusEqual       IS NULL
             OR w.skyStatus      = :skyStatusEqual)
        AND (:precipitationTypeEq  IS NULL
             OR w.precipitationType = :precipitationTypeEq)
        AND (
             :cursorCreatedAt IS NULL
          OR (f.createdAt < :cursorCreatedAt
           OR (f.createdAt = :cursorCreatedAt AND f.id < :cursorId))
        )
      ORDER BY f.createdAt DESC, f.id DESC
      """)
	List<Feed> findFeedsByCreatedAtCursor(
		@Param("keywordLike")           String keywordLike,
		@Param("skyStatusEqual")        SkyStatus skyStatusEqual,
		@Param("precipitationTypeEq")   PrecipitationType precipitationTypeEq,
		@Param("cursorCreatedAt")       Instant cursorCreatedAt,
		@Param("cursorId")              UUID cursorId,
		Pageable pageable
	);

	/** 좋아요순 커서 페이징 + 동일 필터 */
	@Query("""
      SELECT f
      FROM Feed f
      JOIN FETCH f.user u
      JOIN FETCH f.weather w
      WHERE (:keywordLike          IS NULL
             OR LOWER(f.content) LIKE LOWER(CONCAT('%', :keywordLike, '%')))
        AND (:skyStatusEqual       IS NULL
             OR w.skyStatus      = :skyStatusEqual)
        AND (:precipitationTypeEq  IS NULL
             OR w.precipitationType = :precipitationTypeEq)
        AND (
             :cursorLikeCount IS NULL
          OR (f.likeCount < :cursorLikeCount
           OR (f.likeCount = :cursorLikeCount AND f.id < :cursorId))
        )
      ORDER BY f.likeCount DESC, f.id DESC
      """)
	List<Feed> findFeedsByLikeCountCursor(
		@Param("keywordLike")           String keywordLike,
		@Param("skyStatusEqual")        SkyStatus skyStatusEqual,
		@Param("precipitationTypeEq")   PrecipitationType precipitationTypeEq,
		@Param("cursorLikeCount")       Long cursorLikeCount,
		@Param("cursorId")              UUID cursorId,
		Pageable pageable
	);

	/** 전체 개수 조회용 (필터만 적용) */
	@Query("""
      SELECT COUNT(f)
      FROM Feed f
      JOIN f.weather w
      WHERE (:keywordLike          IS NULL
             OR LOWER(f.content) LIKE LOWER(CONCAT('%', :keywordLike, '%')))
        AND (:skyStatusEqual       IS NULL
             OR w.skyStatus      = :skyStatusEqual)
        AND (:precipitationTypeEq  IS NULL
             OR w.precipitationType = :precipitationTypeEq)
      """)
	long countByFilters(
		@Param("keywordLike")           String keywordLike,
		@Param("skyStatusEqual")        SkyStatus skyStatusEqual,
		@Param("precipitationTypeEq")   PrecipitationType precipitationTypeEq
	);
}
