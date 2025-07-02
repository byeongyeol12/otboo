package com.codeit.otboo.domain.notification.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.dto.NotificationDtoCursorResponse;
import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.notification.entity.NotificationLevel;
import com.codeit.otboo.domain.notification.mapper.NotificationMapper;
import com.codeit.otboo.domain.notification.repository.NotificationRepository;
import com.codeit.otboo.domain.sse.service.SseEmitterService;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
	private final NotificationRepository notificationRepository;
	private final NotificationMapper notificationMapper;
	private final UserRepository userRepository;
	private final SseEmitterService sseEmitterService;

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

	// 알림 생성 + 전송
	@Override
	public NotificationDto createAndSend(UUID receiverId, String title, String content, NotificationLevel level) {
		// 알림 받는 사람, 알림 생성
		User receiver = userRepository.findById(receiverId);
		Notification notification = new Notification(
			receiver,title,content,level,false
		);

		NotificationDto notificationDto = notificationMapper.toNotificationDto(notification);
		// 알림 저장
		notificationRepository.save(notification);

		// 알림 전송
		sseEmitterService.send(receiverId,"notifications",notificationDto);

		//반환
		return notificationDto;
	}

	@Override
	public NotificationDtoCursorResponse getNotifications(UUID userId, String cursor, String idAfter, int limit) {

	}

}
