package com.codeit.otboo.domain.user.dto.response;

import java.util.UUID;

import com.codeit.otboo.global.enumType.Gender;

public record ProfileDto(
	UUID userId,
	String name,
	Gender gender,
	String birthDate,
	Object location,
	Integer temperatureSensitivity,
	String profileImageUrl
) {
}
