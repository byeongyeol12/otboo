package com.codeit.otboo.domain.follow.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.codeit.otboo.domain.follow.entity.Follow;
import com.codeit.otboo.domain.follow.entity.User;

public interface FollowRepository extends JpaRepository<Follow, UUID> {
	boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId); // 중복 팔로우 방지 체크

	@Query("""
		SELECT f FROM Follow f
		JOIN f.following u
		WHERE f.follower.id = :followerId
			AND (:idAfter IS NULL OR f.id > :idAfter)
			AND (:nameLike IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%',:nameLIke,'%')))
		ORDER BY f.id ASC
	""")
	List<Follow> findFollowings(UUID followerId, UUID idAfter, String nameLike,Pageable pageable); // 내가 팔로우하는 사람들

	@Query("""
    SELECT f FROM Follow f
    JOIN f.following u
    WHERE f.following.id = :followeeId
      AND (:idAfter IS NULL OR f.id > :idAfter)
      AND (:nameLike IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :nameLike, '%')))
    ORDER BY f.id ASC
""")
	List<Follow> findFollowers(UUID followeeId, UUID idAfter, String nameLike, Pageable pageable); // 나를 팔로우하는 사람들

	long countByFollowing(User user); // 나를 팔로우하는 사람 수
	long countByFollower(User user); // 내가 팔로우하는 사람 수

	Optional<Follow> findByFollowerAndFollowing(User follower, User following);

	boolean existsByFollowerAndFollowing(User follower, User following);
}
