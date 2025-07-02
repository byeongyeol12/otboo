package com.codeit.otboo.domain.user.dto.response;

import java.util.UUID;

import com.codeit.otboo.domain.user.entity.User;

public record UserIdResponse(
	UUID userId
) {
	public static UserIdResponse from(User user) {
		return new UserIdResponse(user.getId());
	}
}