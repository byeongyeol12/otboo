package com.codeit.otboo.domain.follow.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codeit.otboo.domain.follow.entity.Follow;
import com.codeit.otboo.domain.user.entity.User;

public interface FollowRepository extends JpaRepository<Follow, UUID> {
	boolean existsByFollowerAndFollowing(User follwer,User following); // 중복 팔로우 방지 체크
}
