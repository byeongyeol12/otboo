package com.codeit.otboo.domain.user.dto.request;

import java.time.LocalDate;
import com.codeit.otboo.domain.location.dto.response.LocationResponse;
import com.codeit.otboo.global.enumType.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;

public record ProfileUpdateRequest(
	String nickname,
	Gender gender,
	@JsonFormat(pattern = "yyyy-MM-dd")
	LocalDate birthDate,
	LocationResponse location,
	Integer temperatureSensitivity
) {
}
