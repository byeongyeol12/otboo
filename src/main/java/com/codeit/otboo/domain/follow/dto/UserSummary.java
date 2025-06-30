package com.codeit.otboo.domain.follow.dto;

import java.util.UUID;

//임시로 사용
public record UserSummary(
	UUID id,
	String name,
	String profileImageUrl
) {
}
