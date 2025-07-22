package com.codeit.otboo.domain.sse.util;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.sse.service.SseEmitterService;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SseHandler {
	private final SseEmitterService sseEmitterService;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(NotificationCreatedEvent event){
		NotificationDto notificationDto = event.notificationDto();
		UUID userId = notificationDto.receiverId();
		try{
			log.info("[SSE handle - 알림 전송 시도] receiverId={}, notificationId={}", userId, notificationDto.id());
			//SseMessage 객체로 변환
			SseMessage sseMessage = SseMessage.create(userId,"notifications",notificationDto);
			//SSE 알림 송신 서비스 호출
			sseEmitterService.send(sseMessage);
			log.info("[SSE handle - 알림 전송 성공] receiverId={}, notificationId={}", userId, notificationDto.id());
		}catch(Exception e){
			log.error("[SSE handle - 알림 전송 실패] receiverId={}, notificationId={}, error={}",
				userId, notificationDto.id(), e.getMessage(), e);
			throw new CustomException(ErrorCode.SSE_HANDLER_FAILED);
		}
	}
}
