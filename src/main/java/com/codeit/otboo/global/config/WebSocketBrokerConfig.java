package com.codeit.otboo.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.messaging.access.intercept.AuthorizationChannelInterceptor;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.codeit.otboo.domain.websocket.WebSocketAuthInterceptor;
import com.codeit.otboo.global.config.jwt.JwtTokenProvider;
import com.codeit.otboo.global.enumType.Role;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker // 웹소켓브로커를 사용하도록 정의
@RequiredArgsConstructor
public class WebSocketBrokerConfig implements WebSocketMessageBrokerConfigurer {
	private final JwtTokenProvider jwtTokenProvider;
	private final RoleHierarchy roleHierarchy;

	// 클라이언트가 웹소켓 서버에 연결할 수 있는 엔트포인트 설정
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry){
		registry
			.addEndpoint("/ws") // 연결될 엔드 포인트(url = ws://localhost:8080/ws)
			.setAllowedOrigins("http://localhost:8080")// CORS 허용
			.withSockJS(); //WebSocket을 지원하지 않는 브라우저 환경에서도 통신이 가능하도록 SockJS fallback을 설정
	}

	// 클라이언트로부터의 메시지를 처리하고 응답을 전달하는 방법 설정
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry){
		// /sub : 메시지를 구독(수신)하는 요청 엔드포인트
		registry.enableSimpleBroker("/sub");
		// /pub : 메시지를 발행(송신)하는 엔드포인트
		registry.setApplicationDestinationPrefixes("/pub");
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration){
		//웹 소켓 인가
		AuthorizationChannelInterceptor auth = new AuthorizationChannelInterceptor(
			messageAuthorityAuthorizationManager()
		);
		registration.interceptors(
			new WebSocketAuthInterceptor(jwtTokenProvider,roleHierarchy),
			new SecurityContextChannelInterceptor(),
			auth
		);
	}

	public AuthorizationManager<Message<?>> messageAuthorityAuthorizationManager(){
		return MessageMatcherDelegatingAuthorizationManager.builder()
			.anyMessage().hasRole(Role.USER.name())
			.build();
	}
}
