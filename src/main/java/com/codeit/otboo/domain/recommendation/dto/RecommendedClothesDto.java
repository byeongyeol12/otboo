package com.codeit.otboo.domain.recommendation.dto;

import com.codeit.otboo.domain.clothes.dto.response.ClothesAttributeWithDefDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "추천 의상 DTO")
public record RecommendedClothesDto(

		@Schema(description = "의상 ID", example = "a0bb2e60-5d98-48ea-a2a8-6c2cb62c67a9")
		UUID clothesId,

		@Schema(description = "의상 이름", example = "화이트 셔츠")
		String name,

		@Schema(description = "의상 이미지 URL", example = "https://cdn.example.com/clothes/123.jpg")
		String imageUrl,

		@Schema(description = "의상 타입(예: TOP, BOTTOM, OUTER 등)", example = "TOP")
		String type,

		@Schema(description = "의상 속성 리스트")
		List<ClothesAttributeWithDefDto> attributes

) { }
