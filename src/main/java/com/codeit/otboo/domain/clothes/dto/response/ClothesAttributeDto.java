package com.codeit.otboo.domain.clothes.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "의류 속성 값 DTO")
public record ClothesAttributeDto(

		@Schema(description = "속성 정의 ID", example = "4e444ac0-5ec5-41b4-bacf-9c6d2dc0b893")
		UUID definitionId,

		@Schema(description = "속성 값", example = "화이트")
		String value

) { }
