package com.codeit.otboo.domain.clothes.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record ClothesAttributeDefUpdateRequest(
	@NotNull(message = "속성 이름은 필수입니다.")
	String name,

	@NotNull(message = "선택 가능 값 목록은 필수입니다.")
	List<String> selectableValues
) {
}
