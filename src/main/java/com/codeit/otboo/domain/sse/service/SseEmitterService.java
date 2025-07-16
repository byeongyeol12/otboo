package com.codeit.otboo.domain.sse.service;

import java.util.UUID;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.codeit.otboo.domain.sse.util.SseMessage;

public interface SseEmitterService {
	SseEmitter subscribe(UUID receiverId, UUID lastEventId);
	void send(UUID receiverId, String eventName, Object data);
	void send(SseMessage sseMessage);
}
