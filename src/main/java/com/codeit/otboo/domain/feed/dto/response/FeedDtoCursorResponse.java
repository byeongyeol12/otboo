package com.codeit.otboo.domain.feed.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "피드(게시글) 목록 페이지네이션 응답 DTO")
public class FeedDtoCursorResponse {

	@Schema(description = "피드(게시글) 데이터 리스트")
	private List<FeedDto> data;

	@Schema(description = "다음 페이지 커서", example = "eyJjcmVhdGVkQXQiOiIyMDI0LT...")
	private String nextCursor;

	@Schema(description = "다음 페이지 첫 피드의 ID", example = "9f2adfa0-40b6-4700-9e0f-bd8e123c2a76")
	private UUID nextIdAfter;

	@Schema(description = "다음 페이지가 존재하는지 여부", example = "true")
	private boolean hasNext;

	@Schema(description = "전체 데이터 개수", example = "35")
	private long totalCount;

	@Schema(description = "정렬 기준 필드명", example = "createdAt")
	private String sortBy;

	@Schema(description = "정렬 방향 (ASC: 오름차순, DESC: 내림차순)", example = "DESC")
	private String sortDirection;

	public FeedDtoCursorResponse(List<FeedDto> data, String nextCursor, UUID nextIdAfter,
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
