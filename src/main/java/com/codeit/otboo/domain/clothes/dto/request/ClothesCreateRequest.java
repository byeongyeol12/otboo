package com.codeit.otboo.domain.clothes.dto.request;

import com.codeit.otboo.domain.clothes.dto.response.ClothesAttributeDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Schema(description = "의류 등록 요청 DTO")
public record ClothesCreateRequest(

		@Schema(description = "소유자(사용자) ID", example = "a8efc13e-9a11-41fa-bf68-994de9e29c7a")
		@NotNull(message = "사용자 ID는 필수입니다.")
		UUID ownerId,

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
