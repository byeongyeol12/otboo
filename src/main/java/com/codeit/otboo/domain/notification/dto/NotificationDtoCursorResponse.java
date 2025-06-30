package com.codeit.otboo.domain.notification.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record NotificationDtoCursorResponse(
	@NotNull
	List<NotificationDto> data,
	String nextCursor,
	String nextIdAfter,
	@NotNull
	boolean hasNext,
	@NotNull
	Long totalCount,
	String sortBy,
	String sortDirection
) {
}
