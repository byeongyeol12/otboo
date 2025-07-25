package com.codeit.otboo.domain.feed.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "댓글 목록 페이지네이션 응답 DTO")
public class CommentDtoCursorResponse {

	@Schema(description = "댓글 데이터 리스트")
	private List<CommentDto> data;

	@Schema(description = "다음 페이지 커서(댓글 생성 시각, ISO8601)", example = "2024-07-25T11:40:31.000Z")
	private Instant nextCursor;

	@Schema(description = "다음 페이지 첫 댓글의 ID", example = "cfa170e1-58b5-4e6c-9e99-0675674c13b7")
	private UUID nextIdAfter;

	@Schema(description = "다음 페이지가 존재하는지 여부", example = "true")
	private boolean hasNext;

	@Schema(description = "전체 댓글 개수", example = "132")
	private long totalCount;

	@Schema(description = "정렬 기준 필드명", example = "createdAt")
	private String sortBy;

	@Schema(description = "정렬 방향 (ASC: 오름차순, DESC: 내림차순)", example = "DESC")
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
