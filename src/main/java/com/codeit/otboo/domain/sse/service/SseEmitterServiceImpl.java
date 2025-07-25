package com.codeit.otboo.domain.sse.service;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.codeit.otboo.domain.sse.util.SseMessage;
import com.codeit.otboo.domain.sse.repository.SseEmitterRepository;
import com.codeit.otboo.domain.sse.repository.SseMessageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseEmitterServiceImpl implements SseEmitterService {

	@Value("300000") //300,000 milliseconds, 5분
	private long timeout;

	private final SseEmitterRepository sseEmitterRepository;
	private final SseMessageRepository sseMessageRepository;

	//sse 를 통한 구독 기능 정의
	@Override
	@Transactional(readOnly = true)
	public SseEmitter subscribe(UUID receiverId, UUID lastEventId) {
		SseEmitter sseEmitter = new SseEmitter(timeout);

		//클라이언트가 연결을 끊었을 때
		sseEmitter.onCompletion(() -> {
			log.debug("sse on onCompletion");
			sseEmitterRepository.delete(receiverId, sseEmitter);
		});
		//타임아웃으로 끊길 때
		sseEmitter.onTimeout(() -> {
			log.debug("sse on onTimeout");
			sseEmitterRepository.delete(receiverId, sseEmitter);
		});
		//예외/에러 발생
		sseEmitter.onError((e) -> {
			log.debug("sse on onError");
			sseEmitterRepository.delete(receiverId, sseEmitter);
		});

		sseEmitterRepository.save(receiverId, sseEmitter);

		// 마지막 수신 이벤트 ID 이후의 알림 메시지 찾아서 전송
		Optional.ofNullable(lastEventId)
			.ifPresent(id -> {
				sseMessageRepository.findAllByEventIdAfterAndReceiverId(id, receiverId)
					.forEach(sseMessage -> {
						try {
							sseEmitter.send(sseMessage.toEvent());
						} catch (IOException e) {
							log.error("[SSE subscribe - 미수신 알림 복구 실패] receiverId={}, eventId={}, error={}", receiverId, sseMessage.getEventId(), e.getMessage(), e);
						}
					});
			});

		return sseEmitter;
	}

	//1명에게 알림 전송
	@Override
	@Transactional
	public void send(UUID receiverId, String eventName, Object data) {
		sseEmitterRepository.findByReceiverId(receiverId)
			.ifPresent(sseEmitters -> {
				SseMessage message = sseMessageRepository.save(
					SseMessage.create(receiverId, eventName, data));
				sseEmitters.forEach(sseEmitter -> {
					try {
						sseEmitter.send(message.toEvent());
					} catch (IOException e) {
						log.error(e.getMessage(), e);
					}
				});
			});
	}

	@Override
	@Transactional
	public void send(SseMessage sseMessage) {
		// 메모리 큐에 저장(재전송 대비)
		sseMessageRepository.save(sseMessage);
		Set<ResponseBodyEmitter.DataWithMediaType> event = sseMessage.toEvent();

		if (sseMessage.isBroadcast()) {// 전체 broadcast
			sseEmitterRepository.findAll()
				.forEach(sseEmitter -> {
					try {
						sseEmitter.send(event);
					} catch (IOException e) {
						log.error("[SSE send - broadcast 전송 실패] eventId={}, error={}", sseMessage.getEventId(), e.getMessage(),
							e);
					}
				});
		} else { // 특정 유저만
			sseEmitterRepository.findAllByReceiverIdsIn(sseMessage.getReceiverIds())
				.forEach(sseEmitter -> {
					try {
						sseEmitter.send(event);
					} catch (IOException e) {
						log.error("[SSE send - 개별 전송 실패] receiverIds={}, eventId={}, error={}",
							sseMessage.getReceiverIds(), sseMessage.getEventId(), e.getMessage(), e);
					}
				});
		}
	}

	@Scheduled(cron = "0 */1 * * * *")
	public void ping() {
		Set<ResponseBodyEmitter.DataWithMediaType> ping = SseEmitter.event()
			.name("ping")
			.build();
		sseEmitterRepository.findAll()
			.forEach(sseEmitter -> {
				try {
					sseEmitter.send(ping);
				} catch (IOException e) {
					log.error(e.getMessage(), e);
					sseEmitter.completeWithError(e);
				}
			});

	}
}
