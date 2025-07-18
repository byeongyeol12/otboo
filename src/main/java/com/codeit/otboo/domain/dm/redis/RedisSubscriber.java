package com.codeit.otboo.domain.dm.redis;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import com.codeit.otboo.domain.dm.dto.DirectMessageDto;
import com.codeit.otboo.domain.dm.util.DmKeyUtil;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.error.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class RedisSubscriber implements MessageListener {

	private final SimpMessageSendingOperations messagingTemplate; //WebSocket 메시지 push
	private final ObjectMapper objectMapper; // JSON 직렬화 및 역직렬화

	@Override
	public void onMessage(Message message, byte[] pattern) {
		log.info("[RedisSubscriber] onMessage 진입: {}", new String(message.getBody()));
		try{
			// 1. Redis에서 받은 메시지는 직렬화된 JSON 문자열
			String json = new String(message.getBody());

			// 2. DirectMessageDto 로 역직렬화
			DirectMessageDto dmMessage = objectMapper.readValue(json, DirectMessageDto.class);

			// 3. DM Key 생성(= 방 역할)
			String dmKey = DmKeyUtil.makeDmKey(dmMessage.sender().id(),dmMessage.receiver().id());
			String destination = "/sub/direct-messages_" + dmKey;

			log.info("[RedisSubscriber] destination: {}", destination);


			// 4. 웹소켓으로 연결된 구독자에게 실시간 전송
			messagingTemplate.convertAndSend(destination,dmMessage);
			log.info("[RedisSubscriber] Message received : dmKey = {} ", dmKey);

		} catch (Exception e) {
			log.error("[RedisSubscriber] 메시지 처리 중 오류 : ", e.getMessage());
			throw new CustomException(ErrorCode.DM_Redis_MESSAGE_ERROR,e.getMessage());
		}
	}
}
