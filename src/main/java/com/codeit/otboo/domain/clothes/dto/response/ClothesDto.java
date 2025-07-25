package com.codeit.otboo.domain.clothes.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "의류 정보 DTO")
public record ClothesDto(

		@Schema(description = "의류 ID", example = "f0216d30-f135-4ad3-b1e0-4100a49b553d")
		UUID id,

		@Schema(description = "소유자(사용자) ID", example = "a8efc13e-9a11-41fa-bf68-994de9e29c7a")
		UUID ownerId,

		@Schema(description = "의류 이름", example = "블루 진")
		String name,

		@Schema(description = "의류 이미지 URL", example = "https://cdn.example.com/clothes/123.jpg")
		String imageUrl,

		@Schema(description = "의류 타입 (예: TOP, BOTTOM, OUTER 등)", example = "TOP")
		String type,

		@Schema(description = "의류 속성 리스트")
		List<ClothesAttributeWithDefDto> attributes

) { }
