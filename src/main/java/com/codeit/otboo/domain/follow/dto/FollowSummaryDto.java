package com.codeit.otboo.domain.follow.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record FollowSummaryDto(
	@NotNull(message = "팔로이(팔로우 대상) ID 는 필수입니다.")
	UUID followeeId,

	@NotNull
	Long followerCount,

	@NotNull
	Long followingCount,

	@NotNull
	Boolean followerdByMe,

	//내가 해당 사용자를 팔로우 하지 않은 경우 : null
	UUID followedByMeId,

	@NotNull
	Boolean followingMe
) {
}
