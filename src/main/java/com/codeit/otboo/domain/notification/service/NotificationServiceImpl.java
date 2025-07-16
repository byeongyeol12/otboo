package com.codeit.otboo.domain.notification.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.dto.NotificationDtoCursorResponse;
import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.notification.mapper.NotificationMapper;
import com.codeit.otboo.domain.notification.repository.NotificationRepository;
import com.codeit.otboo.domain.sse.util.NotificationCreatedEvent;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
	private final NotificationRepository notificationRepository;
	private final NotificationMapper notificationMapper;
	private final UserRepository userRepository;
	private final ApplicationEventPublisher eventPublisher;

	// 알림 생성 + 전송
	@Override
	public NotificationDto createAndSend(NotificationDto request) {
		log.info("[알림 생성] createAndSend 시작 : receiverId={}, title={}, content={}", request.receiverId(), request.title(), request.content());
		try{
			// 알림 받을 유저 조회
			User receiver = userRepository.findById(request.receiverId()).orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND,"알림 받는 유저를 찾을 수 없습니다."));

			// 알림 엔티티 생성
			Notification notification = Notification.builder()
				.receiver(receiver)
				.title(request.title())
				.content(request.content())
				.level(request.level())
				.confirmed(false)
				.build();
			// 알림 DB 에 저장
			Notification saved = notificationRepository.save(notification);

			log.info("[알림 생성] DB 저장 : receiverId={}, title={}, content={}", saved.getReceiver().getId(), saved.getTitle(), saved.getContent());

			// DTO 변환
			NotificationDto notificationDto = notificationMapper.toNotificationDto(notification);

			// 알림 이벤트 발행
			log.info("[알림 생성] 알림 이벤트 발행 notificationId={}, receiverId={}", notificationDto.id(), notificationDto.receiverId());
			eventPublisher.publishEvent(new NotificationCreatedEvent(notificationDto));

			// 반환
			return notificationDto;
		} catch (Exception e) {
			log.error("[알림 생성 실패] receiverId={}, title={}, content={}, error={}", request.receiverId(), request.title(), request.content(),e.getMessage(),e);
			throw new CustomException(ErrorCode.NOTIFICATION_CREATE_FAILED);
		}
	}


	// 알림 조회
	@Override
	public NotificationDtoCursorResponse getNotifications(UUID receiverId, String cursor, UUID idAfter, int limit) {
		// 커서 변환
		UUID effectiveIdAfter = (cursor != null && !cursor.isBlank()) ? UUID.fromString(cursor) : idAfter;

		// pageable
		Pageable pageable = PageRequest.of(0,limit+1, Sort.Direction.DESC, "createdAt");

		// repository 조회
		List<Notification> list = new ArrayList<>();
		if(effectiveIdAfter != null) {
			list = notificationRepository.findByReceiverIdAndIdGreaterThanOrderByCreatedAt(receiverId,effectiveIdAfter,pageable);
		}else{
			list = notificationRepository.findByReceiverIdAndConfirmedFalse(receiverId,pageable);
		}

		// hasNext,nextCursor,nextIdAfter
		boolean hasNext = list.size() > limit;
		List<Notification> pageList = hasNext ? list.subList(0,limit) : list;
		List<NotificationDto> notificationDtoList = notificationMapper.toNotificationDtoList(pageList);

		UUID nextIdAfter = hasNext && !notificationDtoList.isEmpty() ? notificationDtoList.get(notificationDtoList.size()-1).id() : null;

		String nextCursor = nextIdAfter!= null ? nextIdAfter.toString() : null;

		// 전체 건수
		long totalCount = notificationRepository.countByReceiverId(receiverId);

		// 반환
		return new NotificationDtoCursorResponse(
			notificationDtoList,nextCursor,nextIdAfter,hasNext,totalCount,"createdAt","DESCENDING"
		);
	}
	// 알림 읽음
	@Override
	public void readNotifications(UUID notificationId, UUID receiverId) {
		// 해당 알림 찾음
		Notification notification = notificationRepository.findById(notificationId).orElseThrow(
			()->new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND)
		);

		// 같은 유저인지 확인
		if(!notification.getReceiver().getId().equals(receiverId)) {
			throw new CustomException(ErrorCode.USER_NOT_FOUND,"알림의 유저와 로그인한 유저가 같지 않습니다.");
		}

		// 알림 상태 변경 : false->true
		notification.confirmedChange();

		// 저장
		notificationRepository.save(notification);
	}

}
