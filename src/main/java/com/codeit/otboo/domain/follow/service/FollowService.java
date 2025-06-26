package com.codeit.otboo.domain.follow.service;

import com.codeit.otboo.domain.follow.dto.FollowCreateRequest;
import com.codeit.otboo.domain.follow.dto.FollowDto;

public interface FollowService {
	FollowDto createFollow(FollowCreateRequest request);
}
