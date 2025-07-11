package com.codeit.otboo.domain.user.dto.response;

import java.util.UUID;

import com.codeit.otboo.domain.user.entity.User;

public record UserSummaryDto(
	UUID id,
	String name,
	String profileImageUrl
) {

	public static UserSummaryDto from(User user) {
		return new UserSummaryDto(
			user.getId(),
			user.getName(),
			user.getProfile().getProfileImageUrl()
		);
	}
}
