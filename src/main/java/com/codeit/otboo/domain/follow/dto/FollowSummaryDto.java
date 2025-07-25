package com.codeit.otboo.domain.follow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "팔로우/팔로잉 요약 정보 DTO")
public record FollowSummaryDto(

		@Schema(description = "팔로우 대상 사용자 ID", example = "b5e7ab3f-c73f-4d41-b267-dc92cdd8e42c")
		@NotNull(message = "팔로이(팔로우 대상) ID 는 필수입니다.")
		UUID followeeId,

		@Schema(description = "팔로워(나를 팔로우하는 사람) 수", example = "31")
		@NotNull
		Long followerCount,

		@Schema(description = "팔로잉(내가 팔로우하는 사람) 수", example = "29")
		@NotNull
		Long followingCount,

		@Schema(description = "내가 이 사용자를 팔로우했는지 여부", example = "true")
		@NotNull
		Boolean followedByMe,

		@Schema(description = "내가 해당 사용자를 팔로우하지 않은 경우 null", example = "a2f8b665-c421-43a3-9937-c8820f40357e")
		UUID followedByMeId,

		@Schema(description = "이 사용자가 나를 팔로우하는지 여부", example = "false")
		@NotNull
		Boolean followingMe
) { }
