package com.codeit.otboo.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker // 웹소켓브로커를 사용하도록 정의
@RequiredArgsConstructor
public class WebSocketBrokerConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry){
		registry
			.addEndpoint("/ws") // 연결될 엔드 포인트(url = ws://localhost:8080/ws)
			.setAllowedOrigins("*"); // CORS 허용
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry){
		// /sub : 메시지를 구독(수신)하는 요청 엔드포인트
		registry.enableSimpleBroker("/sub");
		// /pub : 메시지를 발행(송신)하는 엔드포인트
		registry.setApplicationDestinationPrefixes("/pub");

	}
}
