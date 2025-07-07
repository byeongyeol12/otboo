package com.codeit.otboo.domain.user.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.global.enumType.Role;

public record UserDto(
	UUID id,
	Instant createdAt,
	String email,
	String name,
	Role role,
	List<String> linkedOAuthProviders,
	boolean locked
) {
	public static UserDto from(User user) {
		return new UserDto(
			user.getId(),
			user.getCreatedAt(),
			user.getEmail(),
			user.getName(),
			user.getRole(),
			List.of(),
			user.isLocked()
		);
	}
}
