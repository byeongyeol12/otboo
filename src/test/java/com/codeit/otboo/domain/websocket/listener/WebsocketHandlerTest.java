package com.codeit.otboo.domain.websocket.listener;

import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.codeit.otboo.domain.dm.dto.DirectMessageDto;
import com.codeit.otboo.domain.dm.util.DmKeyUtil;
import com.codeit.otboo.domain.dm.websocket.NewDmEvent;
import com.codeit.otboo.domain.dm.websocket.WebsocketHandler;
import com.codeit.otboo.domain.user.dto.response.UserSummaryDto;

@ExtendWith(SpringExtension.class)
public class WebsocketHandlerTest {
	@Mock
	private SimpMessagingTemplate messagingTemplate;
	@InjectMocks
	private WebsocketHandler websocketHandler;

	@Test
	@DisplayName("handleMessage - DM 이벤트 발생 시 정상 동작")
	void handleMessage_success(){
		//given
		UUID senderId = UUID.randomUUID();
		UUID receiverId = UUID.randomUUID();
		UserSummaryDto sender = new UserSummaryDto(senderId, "sender", null);
		UserSummaryDto receiver = new UserSummaryDto(receiverId, "receiver", null);

		DirectMessageDto messageDto = new DirectMessageDto(
			UUID.randomUUID(), Instant.now(),sender,receiver,"test-content"
		);
		NewDmEvent event = new NewDmEvent(messageDto);

		String dmKey = DmKeyUtil.makeDmKey(senderId, receiverId);
		String destination = "/sub/direct-messages_" + dmKey;

		//when
		websocketHandler.handleMessage(event);

		//then
		verify(messagingTemplate,times(1)).convertAndSend(eq(destination), eq(messageDto));
	}
}
