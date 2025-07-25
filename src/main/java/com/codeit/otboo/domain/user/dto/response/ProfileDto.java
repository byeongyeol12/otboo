package com.codeit.otboo.domain.user.dto.response;

import com.codeit.otboo.global.enumType.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "사용자 프로필 정보 DTO")
public record ProfileDto(

		@Schema(description = "사용자 ID", example = "cfc3b4be-b96d-4efb-a1a6-1f0bcd33044e")
		UUID userId,

		@Schema(description = "이름", example = "홍길동")
		String name,

		@Schema(description = "성별 (MALE: 남성, FEMALE: 여성, OTHER: 기타)", example = "MALE")
		Gender gender,

		@Schema(description = "생년월일 (yyyy-MM-dd)", example = "1995-05-17")
		@JsonFormat(pattern = "yyyy-MM-dd")
		LocalDate birthDate,

		@Schema(description = "거주 지역 정보")
		LocationDto location,

		@Schema(description = "평소 온도 민감도 (-5 ~ 5, 0이 일반)", example = "0")
		int temperatureSensitivity,

		@Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/profiles/123.jpg")
		String profileImageUrl

) { }
