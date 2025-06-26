package com.codeit.otboo.domain.clothes.dto.request;

import java.util.List;

import com.codeit.otboo.domain.clothes.dto.response.ClothesAttributeDto;

public record ClothesUpdateRequest(
	String name,
	String type,
	List<ClothesAttributeDto> attributes
) {
}
