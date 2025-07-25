package com.codeit.otboo.domain.clothes.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "의류 속성 정의 DTO")
public record ClothesAttributeDefDto(

		@Schema(description = "속성 정의 ID", example = "4e444ac0-5ec5-41b4-bacf-9c6d2dc0b893")
		UUID id,

		@Schema(description = "속성 이름", example = "색상")
		String name,

		@Schema(description = "선택 가능 값 목록", example = "[\"화이트\", \"블랙\", \"네이비\"]")
		List<String> selectableValues

) { }
