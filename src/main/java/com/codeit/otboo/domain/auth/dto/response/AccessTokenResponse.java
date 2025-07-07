package com.codeit.otboo.domain.auth.dto.response;

import com.codeit.otboo.domain.user.dto.response.UserDto;

public record AccessTokenResponse(
	String accessToken
) {
}
