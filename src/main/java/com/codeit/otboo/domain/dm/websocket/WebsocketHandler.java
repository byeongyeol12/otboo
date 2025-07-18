package com.codeit.otboo.domain.dm.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.codeit.otboo.domain.dm.dto.DirectMessageDto;
import com.codeit.otboo.domain.dm.util.DmKeyUtil;
import com.codeit.otboo.domain.user.dto.response.UserSummaryDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebsocketHandler {
	private final SimpMessagingTemplate messagingTemplate;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleMessage(NewDmEvent event) {
		UserSummaryDto sender = event.dmDto().sender();
		UserSummaryDto receiver = event.dmDto().receiver();
		String dmKey = DmKeyUtil.makeDmKey(sender.id(), receiver.id());

		log.info("[WebSocketHandler] 메시지 수신 : {}", event.dmDto());
		DirectMessageDto messageDto = event.dmDto();
		String destination = String.format("/sub/direct-messages_" + dmKey);
		messagingTemplate.convertAndSend(destination, messageDto);
	}

}
