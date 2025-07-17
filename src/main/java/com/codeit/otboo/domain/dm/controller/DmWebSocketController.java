package com.codeit.otboo.domain.dm.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.codeit.otboo.domain.dm.dto.DirectMessageCreateRequest;
import com.codeit.otboo.domain.dm.dto.DirectMessageDto;
import com.codeit.otboo.domain.dm.service.DmService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DmWebSocketController {

	private final DmService dmService;

	@MessageMapping("/direct-messages_send")
	public DirectMessageDto sendDirectMessage(@Payload DirectMessageCreateRequest directMessageCreateRequest) {
		log.info("텍스트 메시지 생성 요청 : request={}", directMessageCreateRequest);
		DirectMessageDto createDm = dmService.sendDirectMessage(directMessageCreateRequest);
		log.info("텍스트 메시지 생성 응답 : request={}", createDm);
		return createDm;
	}
}
