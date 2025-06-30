package com.codeit.otboo.domain.notification.dto;

import java.time.Instant;
import java.util.UUID;

import com.codeit.otboo.domain.notification.entity.NotificationLevel;

import jakarta.validation.constraints.NotNull;

public record NotificationDto(
	@NotNull(message = "알림 id는 필수입니다.")
	UUID id,
	@NotNull(message = "알림 생성시간은 필수입니다.")
	Instant createdAt,
	@NotNull(message = "알림 수신자는 필수입니다.")
	UUID receiverId,
	@NotNull(message = "알림 제목은 필수입니다.")
	String title,
	@NotNull(message = "알림 내용은 필수입니다.")
	String content,
	@NotNull(message = "알림 레벨은 필수입니다.")
	NotificationLevel level
) {

}
