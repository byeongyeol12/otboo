package com.codeit.otboo.domain.follow.service;

import java.util.UUID;

import com.codeit.otboo.domain.follow.dto.FollowCreateRequest;
import com.codeit.otboo.domain.follow.dto.FollowDto;
import com.codeit.otboo.domain.follow.dto.FollowListResponse;
import com.codeit.otboo.domain.follow.dto.FollowSummaryDto;

public interface FollowService {
	FollowDto createFollow(FollowCreateRequest request); // 팔로우 생성
	FollowSummaryDto getFollowSummary(UUID id,UUID myUserId);
	FollowListResponse getFollowings(UUID followerId,String cursor,UUID idAfter,int limit,String nameLike,String sortBy,String sortDirection); // 내가 팔로우 하는 사람들 목록 조회
	FollowListResponse getFollowers(UUID followeeId,String cursor,UUID idAfter,int limit,String nameLike,String sortBy,String sortDirection); // 나를 팔로우하는 사람들 목록 조회
	void cancelFollow(UUID followId, UUID loginUserId); // 팔로우 취소
}
