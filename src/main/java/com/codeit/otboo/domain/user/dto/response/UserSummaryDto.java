package com.codeit.otboo.domain.user.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.codeit.otboo.domain.user.entity.User;

public record UserSummaryDto(
	UUID id,
	Instant createdAt,
	String email,
	String name,
	String role,
	List<String> linkedOAuthProviders,
	boolean locked
) {
	public static UserSummaryDto from(User user) {
		return new UserSummaryDto(
			user.getId(),
			user.getCreatedAt(),
			user.getEmail(),
			user.getName(),
			user.getRole().name(),
			List.of(),
			user.isLocked()
		);
	}
}
