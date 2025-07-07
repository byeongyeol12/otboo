package com.codeit.otboo.domain.user.dto.response;

import java.util.UUID;

public record UserSummaryDto(
	UUID id,
	String name,
	String profileImageUrl
) {

}
