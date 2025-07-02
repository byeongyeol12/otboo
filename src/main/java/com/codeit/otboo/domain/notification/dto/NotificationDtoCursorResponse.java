package com.codeit.otboo.domain.notification.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record NotificationDtoCursorResponse(
	@NotNull
	List<NotificationDto> data,
	String nextCursor,
	UUID nextIdAfter,
	@NotNull
	boolean hasNext,
	@NotNull
	int totalCount,
	String sortBy,
	String sortDirection
) {
}
