package com.codeit.otboo.domain.user.dto.response;

import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.global.enumType.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "사용자 상세 정보 응답 DTO")
public record UserDto(
		@Schema(description = "사용자 고유 ID", example = "a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6")
		UUID id,

		@Schema(description = "계정 생성 시각")
		Instant createdAt,

		@Schema(description = "이메일", example = "test@example.com")
		String email,

		@Schema(description = "이름", example = "홍길동")
		String name,

		@Schema(description = "사용자 권한", example = "USER")
		Role role,

		@Schema(description = "연동된 소셜 로그인 목록", example = "[\"KAKAO\", \"GOOGLE\"]")
		List<String> linkedOAuthProviders,

		@Schema(description = "계정 잠금 여부", example = "false")
		boolean locked
) {
	public static UserDto from(User user) {
		return new UserDto(
				user.getId(),
				user.getCreatedAt(),
				user.getEmail(),
				user.getName(),
				user.getRole(),
				List.of(), // TODO: 실제 연동된 소셜 로그인 정보로 교체 필요
				user.isLocked()
		);
	}
}