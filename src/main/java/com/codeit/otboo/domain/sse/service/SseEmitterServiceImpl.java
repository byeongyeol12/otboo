package com.codeit.otboo.domain.sse.service;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
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
	public SseEmitter subscribe(UUID receiverId,UUID lastEventId) {
		SseEmitter sseEmitter = new SseEmitter(timeout);

		//클라이언트가 연결을 끊었을 때
		sseEmitter.onCompletion(() -> {
			log.debug("sse on onCompletion");
			sseEmitterRepository.delete(receiverId,sseEmitter);
		});
		//타임아웃으로 끊길 때
		sseEmitter.onTimeout(() -> {
			log.debug("sse on onTimeout");
			sseEmitterRepository.delete(receiverId,sseEmitter);
		});
		//예외/에러 발생
		sseEmitter.onError((e)-> {
			log.debug("sse on onError");
			sseEmitterRepository.delete(receiverId,sseEmitter);
		});

		sseEmitterRepository.save(receiverId,sseEmitter);

		// 미수신 메시지 복구
		Optional.ofNullable(lastEventId)
			.ifPresent(id -> {
				sseMessageRepository.findAllByEventIdAfterAndReceiverId(id, receiverId)
					.forEach(sseMessage -> {
						try {
							sseEmitter.send(sseMessage.toEvent());
						} catch (IOException e) {
							log.error(e.getMessage(), e);
						}
					});
			});

		return sseEmitter;
	}


	//1명에게 알림 전송
	@Override
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

	public void send(SseMessage sseMessage) {
		sseMessageRepository.save(sseMessage);
		Set<ResponseBodyEmitter.DataWithMediaType> event = sseMessage.toEvent();
		if (sseMessage.isBroadcast()) {
			sseEmitterRepository.findAll()
				.forEach(sseEmitter -> {
					try {
						sseEmitter.send(event);
					} catch (IOException e) {
						log.error(e.getMessage(), e);
					}
				});
		} else {
			sseEmitterRepository.findAllByReceiverIdsIn(sseMessage.getReceiverIds())
				.forEach(sseEmitter -> {
					try {
						sseEmitter.send(event);
					} catch (IOException e) {
						log.error(e.getMessage(), e);
					}
				});
		}
	}

	@Scheduled(cron = "0 */1 * * * *")
	public void ping(){
		Set<ResponseBodyEmitter.DataWithMediaType> ping = SseEmitter.event()
			.name("ping")
			.build();
		sseEmitterRepository.findAll()
			.forEach(sseEmitter -> {
				try{
					sseEmitter.send(ping);
				}catch(IOException e){
					log.error(e.getMessage(), e);
					sseEmitter.completeWithError(e);
				}
			});

	}
}
