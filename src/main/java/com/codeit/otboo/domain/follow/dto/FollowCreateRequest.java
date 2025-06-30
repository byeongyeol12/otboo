package com.codeit.otboo.domain.follow.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record FollowCreateRequest(
	@NotNull(message = "팔로이(팔로우 대상) ID는 필수입니다.")
	UUID followeeId,
	@NotNull(message = "팔로워(팔로우 요청자) ID는 필수입니다.")
	UUID followerId
) {
}
