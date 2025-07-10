package com.codeit.otboo.domain.notification.service;

import java.util.List;
import java.util.UUID;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.dto.NotificationDtoCursorResponse;

public interface NotificationService {
	List<NotificationDto> findUnreceived(UUID receiverId, UUID lastEventId);
	NotificationDtoCursorResponse getNotifications(UUID userId, String cursor, UUID idAfter, int limit);
	NotificationDto createAndSend(NotificationDto notificationDto);
	void readNotifications(UUID notificationId, UUID receiverId);
}
