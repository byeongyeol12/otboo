package com.codeit.otboo.domain.notification.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

	// 알림 조회
	@Override
	public NotificationDtoCursorResponse getNotifications(UUID receiverId, String cursor, UUID idAfter, int limit) {
		// 커서 변환
		UUID effectiveIdAfter = (cursor != null && !cursor.isBlank()) ? UUID.fromString(cursor) : idAfter;

		// pageable
		Pageable pageable = PageRequest.of(0,limit, Sort.Direction.DESC, "createdAt");

		// repository 조회
		List<Notification> list = new ArrayList<>();
		if(effectiveIdAfter != null) {
			list = notificationRepository.findByReceiverIdAndIdGreaterThanOrderByCreatedAt(receiverId,effectiveIdAfter,pageable);
		}else{
			list = notificationRepository.findByReceiverIdAndConfirmedFalse(receiverId,pageable);
		}
		List<NotificationDto> notificationDtoList = notificationMapper.toNotificationDtoList(list);

		// hasNext,nextCursor
		boolean hasNext = list.size() > limit;
		UUID nextIdAfter = hasNext ? notificationDtoList.get(notificationDtoList.size()-1).id() : null;

		// 반환
		return new NotificationDtoCursorResponse(
			notificationDtoList,"string",nextIdAfter,hasNext,notificationDtoList.size(),"createdAt","DESC"
		);
	}

}
