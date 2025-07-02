package com.codeit.otboo.domain.notification.service;

import java.util.List;
import java.util.UUID;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.dto.NotificationDtoCursorResponse;
import com.codeit.otboo.domain.notification.entity.NotificationLevel;

public interface NotificationService {
	List<NotificationDto> findUnreceived(UUID receiverId, UUID lastEventId);
	NotificationDtoCursorResponse getNotifications(UUID userId, String cursor, String idAfter, int limit);
	NotificationDto createAndSend(UUID receiverId, String title, String content, NotificationLevel level);
}
