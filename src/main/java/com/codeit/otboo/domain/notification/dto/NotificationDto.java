package com.codeit.otboo.domain.notification.dto;

import com.codeit.otboo.domain.notification.entity.NotificationLevel;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "알림 DTO")
public record NotificationDto(

		@Schema(description = "알림 ID", example = "f0d19be7-0b2e-4c0b-bd14-81f6517e7e15")
		@NotNull(message = "알림 id는 필수입니다.")
		UUID id,

		@Schema(description = "알림 생성 시간(UTC ISO8601)", example = "2024-07-25T14:15:22Z")
		@NotNull(message = "알림 생성시간은 필수입니다.")
		Instant createdAt,

		@Schema(description = "알림 수신자 ID", example = "a8efc13e-9a11-41fa-bf68-994de9e29c7a")
		@NotNull(message = "알림 수신자는 필수입니다.")
		UUID receiverId,

		@Schema(description = "알림 제목", example = "새 피드 댓글 알림")
		@NotNull(message = "알림 제목은 필수입니다.")
		String title,

		@Schema(description = "알림 내용", example = "누군가 회원님의 피드에 댓글을 남겼습니다.")
		@NotNull(message = "알림 내용은 필수입니다.")
		String content,

		@Schema(description = "알림 레벨(중요도)", example = "INFO")
		@NotNull(message = "알림 레벨은 필수입니다.")
		NotificationLevel level

) {}
