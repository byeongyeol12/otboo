package com.codeit.otboo.domain.clothes.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "의류 목록 페이지네이션 응답 DTO")
public record ClothesDtoCursorResponse(

		@Schema(description = "의류 데이터 리스트")
		List<ClothesDto> data,

		@Schema(description = "다음 페이지 커서", example = "eyJjcmVhdGVkQXQiOiIyMDI0LT...")
		String nextCursor,

		@Schema(description = "다음 페이지 첫 의류의 ID", example = "f0216d30-f135-4ad3-b1e0-4100a49b553d")
		UUID nextIdAfter,

		@Schema(description = "다음 페이지가 존재하는지 여부", example = "true")
		boolean hasNext,

		@Schema(description = "전체 데이터 개수", example = "42")
		long totalCount,

		@Schema(description = "정렬 기준 필드명", example = "createdAt")
		String sortBy,

		@Schema(description = "정렬 방향 (ASC: 오름차순, DESC: 내림차순)", example = "DESC")
		String sortDirection

) { }
