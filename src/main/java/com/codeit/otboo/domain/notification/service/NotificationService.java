package com.codeit.otboo.domain.notification.service;

import java.util.UUID;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.dto.NotificationDtoCursorResponse;

public interface NotificationService {
	NotificationDtoCursorResponse getNotifications(UUID userId, String cursor, UUID idAfter, int limit);
	NotificationDto createAndSend(NotificationDto notificationDto);
	void readNotifications(UUID notificationId, UUID receiverId);
}
