package com.codeit.otboo.domain.sse.util;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.sse.service.SseEmitterService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SseHandler {
	private final SseEmitterService sseEmitterService;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(NotificationCreatedEvent event){
		NotificationDto notificationDto = event.notificationDto();
		UUID userId = notificationDto.receiverId();
		SseMessage sseMessage = SseMessage.create(userId,"notifications",notificationDto);
		sseEmitterService.send(sseMessage);
	}
}
