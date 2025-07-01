package com.codeit.otboo.domain.sse.service;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.codeit.otboo.domain.sse.repository.SseEmitterRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SseEmitterService {

	@Value("300_000")
	private long timeout;

	private final SseEmitterRepository sseEmitterRepository;
	private final SseMessageRepositoy sseMessageRepositoy;
	/*
	sse 를 통한 구독 기능 정의
	 */
	public SseEmitter subscribe(UUID receiverId,UUID lastEventId) {
		SseEmitter sseEmitter = new SseEmitter(timeout);

		//sseEmitter complete(연결 끝)
		sseEmitter.onCompletion(() -> {
			//log.debug("sse on onCompletion");
			sseEmitterRepository.delete(receiverId,sseEmitter);
		});
		//sseEmitter timeout(연결 시간 만료)
		sseEmitter.onTimeout(() -> {
			//log.debug("sse on onTimeout");
			sseEmitterRepository.delete(receiverId,sseEmitter);
		});
		//sseEmitter error(연결 에러)
		sseEmitter.onError((e)-> {
			//log.debug("sse on onError");
			sseEmitterRepository.delete(receiverId,sseEmitter);
		});


		sseEmitterRepository.save(receiverId,sseEmitter);

		//마지막 이벤트
		Optional.ofNullable(lastEventId)
			.ifPresent(id->{
				sseMessageRepository.findAllByEventIdAfterAndReceiverId(id,receiverId)
					.forEach(sseMessage -> {
						try{
							sseEmitter.send(sseMessage.toEvent());
						}catch (IOException e){
							//log.error(e.getMessage(),e);
						}
					});
			});
		return sseEmitter;
	}


}
