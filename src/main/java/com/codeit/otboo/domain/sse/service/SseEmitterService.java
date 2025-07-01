package com.codeit.otboo.domain.sse.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SseEmitterService {

	@Value("300_000")
	private long timeout;

	private final Map<UUID, SseEmitter> userEmitters = new ConcurrentHashMap<>(); // key:UUID,value:SseEmitter 객체,ConcurrentHashMap:스레드 안정성 보장

	/*
	sse 를 통한 구독 기능 정의
	 */
	public SseEmitter subscribe(UUID userId) {
		SseEmitter emitter = new SseEmitter(timeout);

		userEmitters.put(userId, emitter);

		//sseEmitter complete(연결 끝)
		emitter.onCompletion(() -> userEmitters.remove(userId));
		//sseEmitter timeout(연결 시간 만료)
		emitter.onTimeout(() -> userEmitters.remove(userId));
		//sseEmitter error(연결 에러)
		emitter.onError((e)->userEmitters.remove(userId));

		return emitter;
	}


}
