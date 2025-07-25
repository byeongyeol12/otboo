package com.codeit.otboo.domain.user.dto.response;

import com.codeit.otboo.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "사용자 ID 응답 DTO")
public record UserIdResponse(
		@Schema(description = "사용자 ID", example = "a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6")
		UUID userId
) {
	public static UserIdResponse from(User user) {
		return new UserIdResponse(user.getId());
	}
}