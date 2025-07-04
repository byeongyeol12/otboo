package com.codeit.otboo.domain.user.dto.request;

import java.time.Instant;

import com.codeit.otboo.global.enumType.Gender;

public record ProfileUpdateRequest(
	String nickname,
	Gender gender,
	Instant birthDate,
	String locationName,
	Integer temperatureSensitivity
) {
}
