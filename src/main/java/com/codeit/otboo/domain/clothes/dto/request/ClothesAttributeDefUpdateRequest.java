package com.codeit.otboo.domain.clothes.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "의류 속성 정의 수정 요청 DTO")
public record ClothesAttributeDefUpdateRequest(

		@Schema(description = "속성 이름", example = "색상")
		@NotNull(message = "속성 이름은 필수입니다.")
		String name,

		@Schema(description = "선택 가능 값 목록", example = "[\"화이트\", \"블랙\", \"네이비\"]")
		@NotNull(message = "선택 가능 값 목록은 필수입니다.")
		List<String> selectableValues

) { }
