package com.codeit.otboo.domain.user.dto.response;

import java.util.List;
import java.util.UUID;

public record UserDtoCursorResponse(
	List<UserSummaryDto> data,
	String nextCursor,
	UUID nextIdAfter,
	boolean hasNext,
	long totalCount,
	String sortBy,
	String sortDirection
) {
}
