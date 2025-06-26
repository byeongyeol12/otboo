package com.codeit.otboo.domain.clothes.dto.request;

import java.util.List;

public record ClothesAttributeDefCreateRequest(
	String name,
	List<String> selectableValues
) {
}
