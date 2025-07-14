package com.codeit.otboo.domain.feed.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentDtoCursorResponse {

	private List<CommentDto> data;
	private Instant nextCursor;
	private UUID nextIdAfter;
	private boolean hasNext;
	private long totalCount;
	private String sortBy;
	private String sortDirection;

	public CommentDtoCursorResponse(List<CommentDto> data, Instant nextCursor, UUID nextIdAfter,
		boolean hasNext, long totalCount, String sortBy, String sortDirection) {
		this.data = data;
		this.nextCursor = nextCursor;
		this.nextIdAfter = nextIdAfter;
		this.hasNext = hasNext;
		this.totalCount = totalCount;
		this.sortBy = sortBy;
		this.sortDirection = sortDirection;
	}
}
