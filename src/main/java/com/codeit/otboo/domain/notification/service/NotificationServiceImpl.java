package com.codeit.otboo.domain.notification.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.dto.NotificationDtoCursorResponse;
import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.notification.mapper.NotificationMapper;
import com.codeit.otboo.domain.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
	private final NotificationRepository notificationRepository;
	private final NotificationMapper notificationMapper;

	//마지막 알림 이후 미확인 알림 반환
	public List<NotificationDto> findUnreceived(UUID receiverId, UUID lastEventId) {
		List<Notification> list = new ArrayList<>();
		if(lastEventId != null) {
			// 마지막 알림 ID가 있으면, 이후 알림만 가져옴
			list = notificationRepository.findByReceiverIdAndIdGreaterThanOrderByCreatedAt(receiverId,lastEventId);
		}else{
			// 마지막 알림 ID 가 없으면, 아직 읽지 않은 모든 알림을 가져옴
			list = notificationRepository.findByReceiverIdAndConfirmedFalse(receiverId);
		}
		return notificationMapper.toNotificationDtoList(list);
	}

	@Override
	public NotificationDtoCursorResponse getNotifications(UUID userId, String cursor, String idAfter, int limit) {
		return null;
	}
}
