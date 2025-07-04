package com.codeit.otboo.domain.user.dto.request;

import com.codeit.otboo.global.enumType.Gender;

public record ProfileUpdateRequest(
	String name,
	Gender gender,
	String birthDate,
	Object location,
	Integer temperatureSensitivity
) {
}
