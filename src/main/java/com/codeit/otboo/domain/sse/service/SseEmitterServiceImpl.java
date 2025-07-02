package com.codeit.otboo.domain.sse.service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.entity.NotificationLevel;
import com.codeit.otboo.domain.sse.repository.SseEmitterRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SseEmitterServiceImpl implements SseEmitterService {

	@Value("300000") //300,000 milliseconds, 5분
	private long timeout;

	private final SseEmitterRepository sseEmitterRepository;

	//sse 를 통한 구독 기능 정의
	public SseEmitter subscribe(UUID receiverId,List<NotificationDto> missedNotifications) {
		SseEmitter sseEmitter = new SseEmitter(timeout);
		sseEmitterRepository.save(receiverId,sseEmitter);

		//클라이언트가 연결을 끊었을 때
		sseEmitter.onCompletion(() -> {
			//log.debug("sse on onCompletion");
			sseEmitterRepository.delete(receiverId,sseEmitter);
		});
		//타임아웃으로 끊길 때
		sseEmitter.onTimeout(() -> {
			//log.debug("sse on onTimeout");
			sseEmitterRepository.delete(receiverId,sseEmitter);
		});
		//예외/에러 발생
		sseEmitter.onError((e)-> {
			//log.debug("sse on onError");
			sseEmitterRepository.delete(receiverId,sseEmitter);
		});

		//503 error 방지용 dummy event
		send(receiverId,"dummy", new NotificationDto(UUID.randomUUID(), Instant.now(),receiverId,"dummy","dummy", NotificationLevel.INFO));

		//미수신한 Event 목록이 존재할 경우 전송
		for (NotificationDto notificationDto : missedNotifications) {
			send(receiverId,"notifications",notificationDto);
		}

		return sseEmitter;
	}

	//1명에게 알림 전송
	public void send(UUID receiverId, String eventName, NotificationDto notificationDto) {
		List<SseEmitter> sseEmitters = sseEmitterRepository.findByReceiverId(receiverId).orElse(List.of());
		for(SseEmitter sseEmitter : sseEmitters) {
			try {
				sseEmitter.send(
					SseEmitter.event()
						.id(notificationDto.id().toString()) // 알림 고유 ID
						.name(eventName) // 이벤트명
						.data(notificationDto) // 실제 알림 데이터(JSON)
				);
			} catch (IOException e) {
				sseEmitterRepository.delete(receiverId,sseEmitter); // 전송 실패시 emitter 제거
			}
		}
	}

	//시스템 이벤트
	public void broadcast(String eventName, Object data){
		for(SseEmitter emitter : sseEmitterRepository.findAll()){
			try{
				emitter.send(SseEmitter.event().name(eventName).data(data));
			}catch(IOException e){
				//log.error(e.getMessage(),e);
			}
		}
	}

}
