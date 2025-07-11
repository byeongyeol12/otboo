package com.codeit.otboo.domain.clothes.dto.request;

import java.util.List;

import com.codeit.otboo.domain.clothes.dto.response.ClothesAttributeDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClothesCreateRequest(

	@NotBlank(message = "옷 이름은 필수입니다.")
	String name,

	@NotBlank(message = "의상 타입은 필수입니다.")
	String type,

	@NotNull(message = "속성 목록은 필수입니다.")
	List<ClothesAttributeDto> attributes
) {
}
