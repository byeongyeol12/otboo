package com.codeit.otboo.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "사용자 목록 페이지네이션 응답 DTO")
public record UserDtoCursorResponse(

		@Schema(description = "사용자 데이터 리스트")
		List<UserDto> data,

		@Schema(description = "다음 페이지 커서", example = "eyJjcmVhdGVkQXQiOiIyMDI0LT...")
		String nextCursor,

		@Schema(description = "다음 페이지 첫 사용자의 ID", example = "f62b59b1-7251-47c6-b37b-bb5c2e041f92")
		UUID nextIdAfter,

		@Schema(description = "다음 페이지가 있는지 여부", example = "true")
		boolean hasNext,

		@Schema(description = "전체 데이터 개수", example = "152")
		long totalCount,

		@Schema(description = "정렬 기준 필드명", example = "createdAt")
		String sortBy,

		@Schema(description = "정렬 방향 (ASC: 오름차순, DESC: 내림차순)", example = "DESC")
		String sortDirection

) { }
