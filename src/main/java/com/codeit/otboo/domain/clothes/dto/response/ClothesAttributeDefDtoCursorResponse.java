package com.codeit.otboo.domain.clothes.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "의류 속성 정의 목록 페이지네이션 응답 DTO")
public record ClothesAttributeDefDtoCursorResponse(

		@Schema(description = "의류 속성 정의 데이터 리스트")
		List<ClothesAttributeDefDto> data,

		@Schema(description = "다음 페이지 커서", example = "eyJjcmVhdGVkQXQiOiIyMDI0LT...")
		String nextCursor,

		@Schema(description = "다음 페이지 첫 데이터의 ID", example = "4e444ac0-5ec5-41b4-bacf-9c6d2dc0b893")
		UUID nextIdAfter,

		@Schema(description = "다음 페이지가 존재하는지 여부", example = "true")
		boolean hasNext,

		@Schema(description = "전체 데이터 개수", example = "45")
		long totalCount,

		@Schema(description = "정렬 기준 필드명", example = "createdAt")
		String sortBy,

		@Schema(description = "정렬 방향 (ASC: 오름차순, DESC: 내림차순)", example = "DESC")
		String sortDirection

) { }
