package com.codeit.otboo.domain.sse.repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
// 각 사용자 별로 여러 SSE 연결을 메모리에 저장/조회/삭제/전체 조회
public class SseEmitterRepository {
	// 동일 사용자에 대해 여러 SseEmitter 생길 수 있어서 리스트로 관리
	private final Map<UUID, List<SseEmitter>> data = new ConcurrentHashMap<>(); //ConcurrentHashMap:스레드 안정성 보장

	public SseEmitter save(UUID receiverId, SseEmitter sseEmitter) {
		data.putIfAbsent(receiverId,new CopyOnWriteArrayList<>()); //동일 유저 키가 있으면 아무것도 하지 않고, 없으면 새롭게 리스트 생성
		data.get(receiverId).add(sseEmitter);
		return sseEmitter;
	}

	public void delete(UUID receiverId, SseEmitter sseEmitter) {
		if(data.containsKey(receiverId)) {
			data.get(receiverId).remove(sseEmitter);
		}
	}
}
