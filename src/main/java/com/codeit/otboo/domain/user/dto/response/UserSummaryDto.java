package com.codeit.otboo.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import com.codeit.otboo.domain.user.entity.User;

@Schema(description = "간단 사용자 정보 DTO")
public record UserSummaryDto(

		@Schema(description = "사용자 ID", example = "cfc3b4be-b96d-4efb-a1a6-1f0bcd33044e")
		UUID id,

		@Schema(description = "사용자 이름", example = "홍길동")
		String name,

		@Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/profiles/123.jpg")
		String profileImageUrl

) {
	public static UserSummaryDto from(User user) {
		return new UserSummaryDto(
				user.getId(),
				user.getName(),
				user.getProfile().getProfileImageUrl()
		);
	}
}
