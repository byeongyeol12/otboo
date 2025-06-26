package com.codeit.otboo.domain.follow.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codeit.otboo.domain.follow.entity.Follow;

public interface FollowRepository extends JpaRepository<Follow, UUID> {
	boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId); // 중복 팔로우 방지 체크
	List<Follow> findAllByFollowerId(UUID followerId); // 내가 팔로우하는 사람들
	List<Follow> findAllByFollowingId(UUID followeeId); //나를 팔로우하는 사람들
}
