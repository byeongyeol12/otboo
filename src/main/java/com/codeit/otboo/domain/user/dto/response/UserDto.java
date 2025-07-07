package com.codeit.otboo.domain.user.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
}
