package com.codeit.otboo.domain.clothes.dto.request;

import com.codeit.otboo.domain.clothes.dto.response.ClothesAttributeDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "의류 수정 요청 DTO")
public record ClothesUpdateRequest(

		@Schema(description = "옷 이름", example = "블루 진")
		@NotBlank(message = "옷 이름은 필수입니다.")
		String name,

		@Schema(description = "의상 타입 (예: TOP, BOTTOM, OUTER 등)", example = "TOP")
		@NotBlank(message = "의상 타입은 필수입니다.")
		String type,

		@Schema(description = "속성 목록", example = "[{\"definitionId\": \"4e444ac0-5ec5-41b4-bacf-9c6d2dc0b893\", \"value\": \"화이트\"}]")
		@NotNull(message = "속성 목록은 필수입니다.")
		List<ClothesAttributeDto> attributes

) { }
