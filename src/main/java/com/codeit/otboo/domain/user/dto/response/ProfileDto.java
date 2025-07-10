package com.codeit.otboo.domain.user.dto.response;

import java.time.LocalDate;
import java.util.UUID;

import com.codeit.otboo.global.enumType.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;

public record ProfileDto(
	UUID userId,
	String name,
	Gender gender,
	@JsonFormat(pattern = "yyyy-MM-dd")
	LocalDate birthDate,
	LocationDto location,
	int temperatureSensitivity,
	String profileImageUrl
) {
}
