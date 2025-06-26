package com.codeit.otboo.domain.follow.service;

import java.util.List;
import java.util.UUID;

import com.codeit.otboo.domain.follow.dto.FollowCreateRequest;
import com.codeit.otboo.domain.follow.dto.FollowDto;

public interface FollowService {
	FollowDto createFollow(FollowCreateRequest request);
	List<FollowDto> getFollowings(UUID followerId,String cursor,UUID idAfter,int limit,String nameLike); // 내가 팔로우 하는 사람들
	List<FollowDto> getFollowers(UUID followerId,String cursor,UUID idAfter,int limit,String nameLike); // 나를 팔로우하는 사람들
}
