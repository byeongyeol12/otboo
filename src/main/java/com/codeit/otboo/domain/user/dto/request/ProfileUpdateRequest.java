package com.codeit.otboo.domain.user.dto.request;

import com.codeit.otboo.domain.location.dto.response.LocationResponse;
import com.codeit.otboo.global.enumType.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "프로필 수정 요청 DTO")
public record ProfileUpdateRequest(

		@Schema(description = "새로운 닉네임", example = "따뜻한겨울")
		String nickname,

		@Schema(description = "성별", example = "MALE")
		Gender gender,

		@Schema(description = "생년월일", example = "1995-12-25")
		@JsonFormat(pattern = "yyyy-MM-dd")
		LocalDate birthDate,

		@Schema(description = "새로운 위치 정보")
		LocationResponse location,

		@Schema(description = "온도 민감도 (-5 ~ 5 사이의 정수)", example = "3")
		Integer temperatureSensitivity
) {
}