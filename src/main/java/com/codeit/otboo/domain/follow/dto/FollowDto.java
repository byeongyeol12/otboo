package com.codeit.otboo.domain.follow.dto;

import java.util.UUID;

import com.codeit.otboo.domain.user.dto.response.UserSummaryDto;

import jakarta.validation.constraints.NotNull;

public record FollowDto(
	@NotNull(message = "팔로우 ID는 필수입니다.")
	UUID id,
	@NotNull(message = "팔로이(팔로우 대상) 정보는 필수입니다.")
	UserSummaryDto followee,
	@NotNull(message = "팔로워(팔로우 요청자) 정보는 필수입니다.")
	UserSummaryDto follower
) {

}
