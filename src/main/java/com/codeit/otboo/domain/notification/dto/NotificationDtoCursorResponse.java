package com.codeit.otboo.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

@Schema(description = "알림 목록 페이지네이션 응답 DTO")
public record NotificationDtoCursorResponse(

		@Schema(description = "알림 데이터 리스트")
		@NotNull
		List<NotificationDto> data,

		@Schema(description = "다음 페이지 커서", example = "eyJjcmVhdGVkQXQiOiIyMDI0LT...")
		String nextCursor,

		@Schema(description = "다음 페이지 첫 알림의 ID", example = "f0d19be7-0b2e-4c0b-bd14-81f6517e7e15")
		UUID nextIdAfter,

		@Schema(description = "다음 페이지 존재 여부", example = "true")
		@NotNull
		boolean hasNext,

		@Schema(description = "전체 알림 개수", example = "13")
		@NotNull
		long totalCount,

		@Schema(description = "정렬 기준 필드명", example = "createdAt")
		String sortBy,

		@Schema(description = "정렬 방향 (ASC: 오름차순, DESC: 내림차순)", example = "DESC")
		String sortDirection

) { }
