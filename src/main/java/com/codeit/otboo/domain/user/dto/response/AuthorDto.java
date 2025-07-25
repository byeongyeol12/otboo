package com.codeit.otboo.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "작성자 정보 DTO")
public record AuthorDto (
		@Schema(description = "작성자 ID", example = "a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6")
		UUID userId,

		@Schema(description = "작성자 이름", example = "옷잘알")
		String name,

		@Schema(description = "작성자 프로필 이미지 URL", example = "https://example.com/profile.jpg")
		String profileImageUrl
) {}