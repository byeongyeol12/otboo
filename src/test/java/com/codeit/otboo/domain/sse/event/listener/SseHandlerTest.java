package com.codeit.otboo.domain.sse.event.listener;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.entity.NotificationLevel;
import com.codeit.otboo.domain.sse.event.NotificationCreatedEvent;
import com.codeit.otboo.domain.sse.service.SseEmitterService;
import com.codeit.otboo.domain.sse.util.SseMessage;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.error.ErrorCode;

@ExtendWith(SpringExtension.class)
public class SseHandlerTest {
	@Mock
	private SseEmitterService sseEmitterService;

	@InjectMocks
	private SseHandler sseHandler;

	@Test
	@DisplayName("handle - 알림 이벤트 sse 메시지 정상 동작")
	void handle_notification_success(){
		//given
		UUID receiverId = UUID.randomUUID();
		UUID notificationId = UUID.randomUUID();
		NotificationDto notificationDto = new NotificationDto(
			notificationId,Instant.now(),receiverId,"test","test-content", NotificationLevel.INFO
		);
		NotificationCreatedEvent event = new NotificationCreatedEvent(notificationDto);

		//when
		sseHandler.handle(event);

		// then
		ArgumentCaptor<SseMessage> captor = ArgumentCaptor.forClass(SseMessage.class);
		verify(sseEmitterService, times(1)).send(captor.capture());

		SseMessage sentMessage = captor.getValue();

		assertThat(sentMessage.getReceiverIds()).contains(receiverId);
		assertThat(sentMessage.getEventName()).isEqualTo("notifications");
		assertThat(sentMessage.getEventData()).isEqualTo(notificationDto);
		assertThat(sentMessage.isBroadcast()).isFalse();
	}

	@Test
	@DisplayName("handle - 알림 이벤트 수신 실패")
	void handle_notification_Failed() {
		// given
		UUID receiverId = UUID.randomUUID();
		UUID notificationId = UUID.randomUUID();
		NotificationDto notificationDto = new NotificationDto(
			notificationId,Instant.now(),receiverId,"test","test-content", NotificationLevel.INFO
		);

		NotificationCreatedEvent event = new NotificationCreatedEvent(notificationDto);

		doThrow(new RuntimeException("전송 오류")).when(sseEmitterService).send(any(SseMessage.class));

		// when, then
		CustomException ex = catchThrowableOfType(
			() -> sseHandler.handle(event),
			CustomException.class
		);

		assertThat(ex).isNotNull();
		assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SSE_HANDLER_FAILED);

		verify(sseEmitterService, times(1)).send(any(SseMessage.class));
	}
}
