package com.codeit.otboo.domain.dm.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import com.codeit.otboo.domain.dm.dto.DirectMessageCreateRequest;
import com.codeit.otboo.domain.dm.service.DmServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DmWebSocketController {

	private final DmServiceImpl dmService;

	@MessageMapping("/direct-messages_send")
	public DirectMessageCreateRequest sendDirectMessage(DirectMessageCreateRequest directMessageCreateRequest) {
		dmService.sendDirectMessage(directMessageCreateRequest);
	}
}
