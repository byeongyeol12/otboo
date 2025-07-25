package com.codeit.otboo.domain.follow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "팔로우 생성 요청 DTO")
public record FollowCreateRequest(

		@Schema(description = "팔로우 대상 사용자 ID (followee)", example = "6a1b7a7b-8a9d-4e5b-81e1-12f86f87a7a6")
		@NotNull(message = "팔로잉(팔로우 대상) ID는 필수입니다.")
		UUID followeeId,

		@Schema(description = "팔로우 요청자 ID (follower, JWT에서 추출됨)", example = "43e3dbe5-5f52-4fa1-989f-34c8410b64be")
		UUID followerId // JWT 로 추출

) { }
