package com.codeit.otboo.domain.sse.event;

import java.time.Instant;

import com.codeit.otboo.domain.notification.dto.NotificationDto;

public record NotificationCreatedEvent(
	Instant createdAt,
	NotificationDto notificationDto
) {
	public NotificationCreatedEvent(NotificationDto notificationDto){
		this(Instant.now(), notificationDto);
	}
}
