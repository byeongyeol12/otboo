package com.codeit.otboo.domain.recommendation.dto;

import java.util.List;
import java.util.UUID;

import com.codeit.otboo.domain.clothes.dto.response.ClothesAttributeWithDefDto;

public record RecommendedClothesDto(
	UUID clothesId,
	String name,
	String imageUrl,
	String type,
	List<ClothesAttributeWithDefDto> attributes
) {
}
