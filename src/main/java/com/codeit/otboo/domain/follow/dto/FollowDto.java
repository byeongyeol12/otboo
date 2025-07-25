package com.codeit.otboo.domain.follow.dto;

import com.codeit.otboo.domain.user.dto.response.UserSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "팔로우 관계 정보 DTO")
public record FollowDto(
		@Schema(description = "팔로우 관계의 고유 ID", example = "a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6")
		@NotNull(message = "팔로우 ID는 필수입니다.")
		UUID id,

		@Schema(description = "팔로우 당하는 사용자 정보")
		@NotNull(message = "팔로이(팔로우 대상) 정보는 필수입니다.")
		UserSummaryDto followee,

		@Schema(description = "팔로우 하는 사용자 정보")
		@NotNull(message = "팔로워(팔로우 요청자) 정보는 필수입니다.")
		UserSummaryDto follower
) {

}