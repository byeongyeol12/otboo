package com.codeit.otboo.domain.follow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Schema(description = "팔로우 리스트 페이지네이션 응답 DTO")
public record FollowListResponse(

		@Schema(description = "팔로우 데이터 목록")
		@NotNull
		List<FollowDto> data,

		@Schema(description = "다음 페이지 커서", example = "eyJjcmVhdGVkQXQiOiIyMDI0LT...")
		String nextCursor, // 다음 커서

		@Schema(description = "다음 페이지 첫 팔로우 ID", example = "c3f2f6c9-13b7-4b5d-bb6b-8c1f0dca892e")
		UUID nextIdAfter, // 다음 요청의 보조 커서

		@Schema(description = "다음 페이지 존재 여부", example = "true")
		@NotNull
		Boolean hasNext, // 다음 데이터가 있는지 여부

		@Schema(description = "전체 데이터 개수", example = "128")
		@NotNull
		Long totalCount, // 총 데이터 개수

		@Schema(description = "정렬 기준 필드명", example = "createdAt")
		String sortBy, // 정렬 기준

		@Schema(description = "정렬 방향 (ASC: 오름차순, DESC: 내림차순)", example = "DESC")
		String sortDirection // 정렬 방향

) { }
