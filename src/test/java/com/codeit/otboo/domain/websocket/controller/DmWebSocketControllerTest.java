package com.codeit.otboo.domain.websocket.controller;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.codeit.otboo.domain.dm.dto.DirectMessageCreateRequest;
import com.codeit.otboo.domain.dm.dto.DirectMessageDto;
import com.codeit.otboo.domain.dm.service.DmService;
import com.codeit.otboo.domain.user.dto.response.UserSummaryDto;

@ExtendWith(SpringExtension.class)
public class DmWebSocketControllerTest {

	@Mock
	private DmService dmService;
	@InjectMocks
	private DmWebSocketController dmWebSocketController;

	@Test
	@DisplayName("sendDirectMessage - 메시지 전송 성공")
	void sendDirectMessage_success() throws Exception {
		//given
		UUID senderId = UUID.randomUUID();
		UUID receiverId = UUID.randomUUID();

		DirectMessageCreateRequest request = new DirectMessageCreateRequest(
			receiverId,senderId,"test-content"
		);

		DirectMessageDto dto = new DirectMessageDto(
			UUID.randomUUID(),
			Instant.now(),
			new UserSummaryDto(senderId,"sender",null),
			new UserSummaryDto(receiverId,"receiver",null),
			"test-content"
		);

		when(dmService.sendDirectMessage(eq(request))).thenReturn(dto);

		//when
		DirectMessageDto result = dmWebSocketController.sendDirectMessage(request);
		
		// then
		assertThat(result).isEqualTo(dto);
		verify(dmService, times(1)).sendDirectMessage(request);
	}

	@Test
	@DisplayName("sendDirectMessage - 서비스에서 예외 발생 시 전파 되는지 검증")
	void sendDirectMessage_fail() throws Exception {
		//given
		UUID senderId = UUID.randomUUID();
		UUID receiverId = UUID.randomUUID();

		DirectMessageCreateRequest request = new DirectMessageCreateRequest(
			receiverId,senderId,"test-content"
		);

		DirectMessageDto dto = new DirectMessageDto(
			UUID.randomUUID(),
			Instant.now(),
			new UserSummaryDto(senderId,"sender",null),
			new UserSummaryDto(receiverId,"receiver",null),
			"test-content"
		);
		when(dmService.sendDirectMessage(request)).thenThrow(new RuntimeException("Service Error"));

		//when,then
		assertThrows(RuntimeException.class, () -> dmWebSocketController.sendDirectMessage(request));
	}
}
