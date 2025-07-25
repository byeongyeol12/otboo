package com.codeit.otboo.domain.redis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.data.redis.connection.Message;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import com.codeit.otboo.domain.dm.dto.DirectMessageDto;
import com.codeit.otboo.domain.dm.util.DmKeyUtil;
import com.codeit.otboo.domain.user.dto.response.UserSummaryDto;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.error.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class RedisSubscriberTest {
	@Mock
	private SimpMessageSendingOperations messagingTemplate;

	@Mock
	private ObjectMapper objectMapper;

	@InjectMocks
	private RedisSubscriber redisSubscriber;


	@Test
	@DisplayName("onMessage - 메시지 및 웹소켓 정상 성공")
	void onMessage_success() throws Exception {
		//given
		UUID senderId = UUID.randomUUID();
		UUID receiverId = UUID.randomUUID();
		DirectMessageDto dmMessage = new DirectMessageDto(
			UUID.randomUUID(),
			Instant.now(),
			new UserSummaryDto(senderId,"sender","sender.png"),
			new UserSummaryDto(receiverId,"receiver","receiver.png"),
			"test-content"
		);
		String json = "{\"id\":\"" + dmMessage.id() + "\"}";

		when(objectMapper.readValue(anyString(), eq(DirectMessageDto.class))).thenReturn(dmMessage);

		String expectedDmKey = DmKeyUtil.makeDmKey(senderId, receiverId);
		String expectedDestination = "/sub/direct-messages_" + expectedDmKey;

		Message message = new DefaultMessage(
			json.getBytes(StandardCharsets.UTF_8),
			"channel".getBytes(StandardCharsets.UTF_8)
		);

		// when
		redisSubscriber.onMessage(message, null);

		// then
		verify(messagingTemplate, times(1))
			.convertAndSend(eq(expectedDestination), eq(dmMessage));
	}

	@Test
	@DisplayName("onMessage - JSON 역직렬화 실패로 예외 발생")
	void onMessage_JsonError_failed() throws Exception {
		//given
		String json = "{\"id\":\"" + UUID.randomUUID() + "\"}";
		Message message = new DefaultMessage(
			json.getBytes(StandardCharsets.UTF_8),
			"channel".getBytes(StandardCharsets.UTF_8)
		);

		when(objectMapper.readValue(anyString(), eq(DirectMessageDto.class))).thenThrow(new JsonProcessingException("JSON 역직렬화 에러"){});

		//when,then
		CustomException exception = assertThrows(CustomException.class,() ->{
			redisSubscriber.onMessage(message, null);
		});
		assertEquals(ErrorCode.DM_Redis_MESSAGE_ERROR, exception.getErrorCode());
	}
}
