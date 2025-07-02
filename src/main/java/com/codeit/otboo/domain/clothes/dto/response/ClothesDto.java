package com.codeit.otboo.domain.clothes.dto.response;

import java.util.List;
import java.util.UUID;

public record ClothesDto(
	UUID id,
	UUID ownerId,
	String name,
	String imageUrl,
	String type,
	List<ClothesAttributeWithDefDto> attributes
) {
}
