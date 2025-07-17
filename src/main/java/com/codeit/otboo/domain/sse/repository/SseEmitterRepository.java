package com.codeit.otboo.domain.sse.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class SseEmitterRepository {
	// 동일 사용자에 대해 여러 SseEmitter 생길 수 있어서 리스트로 관리
	private final Map<UUID, List<SseEmitter>> data = new ConcurrentHashMap<>(); //ConcurrentHashMap:스레드 안정성 보장

	//emitter 저장
	public SseEmitter save(UUID receiverId, SseEmitter sseEmitter) {
		data.putIfAbsent(receiverId,new CopyOnWriteArrayList<>()); //동일 유저 키가 있으면 아무것도 하지 않고, 없으면 새롭게 리스트 생성
		data.get(receiverId).add(sseEmitter);
		return sseEmitter;
	}

	//receiverId 에 맞는 emitter 반환
	public Optional<List<SseEmitter>> findByReceiverId(UUID receiverId) {
		return Optional.ofNullable(data.get(receiverId));
	}

	public List<SseEmitter> findAllByReceiverIdsIn(Collection<UUID> receiverIds) {
		return data.entrySet().stream()
			.filter(entry->receiverIds.contains(entry.getKey()))
			.map(Map.Entry::getValue)
			.flatMap(Collection::stream)
			.toList();
	}

	//모든 유저의 emitter 반환
	public List<SseEmitter> findAll() {
		return data.values().stream().flatMap(List::stream).toList();
	}

	//emitter 삭제
	public void delete(UUID receiverId, SseEmitter sseEmitter) {
		if(data.containsKey(receiverId)) {
			data.get(receiverId).remove(sseEmitter);
		}
	}
}
