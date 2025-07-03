package com.codeit.otboo.domain.sse.service;

import java.util.List;
import java.util.UUID;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.codeit.otboo.domain.notification.dto.NotificationDto;

public interface SseEmitterService {
	SseEmitter subscribe(UUID receiverId, List<NotificationDto> missedNotifications);
	void send(UUID receiverId, String eventName, NotificationDto notificationDto);
	void broadcast(String eventName, Object data);
}
