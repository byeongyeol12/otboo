package com.codeit.otboo.domain.dm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "쪽지(Direct Message) 목록 페이지네이션 응답 DTO")
public record DirectMessageDtoCursorResponse(

		@Schema(description = "쪽지 데이터 리스트")
		List<DirectMessageDto> data,

		@Schema(description = "다음 페이지 커서", example = "eyJjcmVhdGVkQXQiOiIyMDI0LT...")
		String nextCursor,

		@Schema(description = "다음 페이지 첫 쪽지 ID", example = "e5cd3d2f-95e2-4d33-b6f7-45b6f4a845ed")
		UUID nextIdAfter,

		@Schema(description = "다음 페이지 존재 여부", example = "true")
		boolean hasNext,

		@Schema(description = "전체 쪽지 개수", example = "19")
		long totalCount,

		@Schema(description = "정렬 기준 필드명", example = "createdAt")
		String sortBy,

		@Schema(description = "정렬 방향 (ASC: 오름차순, DESC: 내림차순)", example = "DESC")
		String sortDirection

) { }
