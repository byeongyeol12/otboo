package com.codeit.otboo.domain.follow.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.codeit.otboo.domain.follow.entity.Follow;
import com.codeit.otboo.domain.user.entity.User;

public interface FollowRepository extends JpaRepository<Follow, UUID> {
	boolean existsByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId); // 중복 팔로우 방지 체크

	@Query("""
    SELECT f FROM Follow f
    JOIN f.followee u
    WHERE f.follower.id = :followerId
      AND (:idAfter IS NULL OR f.id > :idAfter)
      AND (:nameLike IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', CAST(:nameLike AS string), '%')))
    ORDER BY f.id ASC
""")
	List<Follow> findFollowees(UUID followerId, UUID idAfter, String nameLike, Pageable pageable); // 내가 팔로우하는 사람들

	@Query("""
    SELECT f FROM Follow f
    JOIN f.follower u
    WHERE f.followee.id = :followeeId
      AND (:idAfter IS NULL OR f.id > :idAfter)
      AND (:nameLike IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', CAST(:nameLike AS string), '%')))
    ORDER BY f.id ASC
""")
	List<Follow> findFollowers(UUID followeeId, UUID idAfter, String nameLike, Pageable pageable); // 나를 팔로우하는 사람들

	long countByFolloweeId(UUID followeeId); // 나를 팔로우하는 사람 수
	long countByFollowerId(UUID followerId); // 내가 팔로우하는 사람 수

	Optional<Follow> findByFollowerAndFollowee(User follower, User followee);

	boolean existsByFollowerAndFollowee(User follower, User followee);

	List<Follow> findAllByFolloweeId(UUID followeeId);
}
