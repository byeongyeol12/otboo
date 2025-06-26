package com.codeit.otboo.domain.clothes.dto.request;

import java.util.List;
import java.util.UUID;

import com.codeit.otboo.domain.clothes.dto.response.ClothesAttributeDto;

public record ClothesCreateRequest(
	UUID ownerId,
	String name,
	String type,
	List<ClothesAttributeDto> attributes
) {
}
