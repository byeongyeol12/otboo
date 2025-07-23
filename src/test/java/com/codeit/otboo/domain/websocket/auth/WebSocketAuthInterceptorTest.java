package com.codeit.otboo.domain.websocket.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.codeit.otboo.domain.dm.websocket.WebSocketAuthInterceptor;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.config.jwt.JwtTokenProvider;
import com.codeit.otboo.global.config.security.UserPrincipal;
import com.codeit.otboo.global.enumType.Role;
import com.codeit.otboo.global.error.ErrorCode;

import io.jsonwebtoken.Claims;

@ExtendWith(SpringExtension.class)
public class WebSocketAuthInterceptorTest {

	@Mock
	private JwtTokenProvider jwtTokenProvider;
	@Mock
	private RoleHierarchy roleHierarchy;
	@InjectMocks
	private WebSocketAuthInterceptor webSocketAuthInterceptor;

	//preSend
	@Test
	@DisplayName("preSend - 토큰 인증 SetUser 등록 성공")
	void preSend_success() {
		//given
		UUID userId = UUID.randomUUID();
		String email = "test@email.com";
		String role = Role.USER.toString();
		String token = "access-token";
		String bearerToken = "Bearer "+token;

		Claims claims = mock(Claims.class);
		when(claims.get("userId", String.class)).thenReturn(userId.toString());
		when(claims.get("email", String.class)).thenReturn(email);
		when(claims.get("role", String.class)).thenReturn(role);

		when(jwtTokenProvider.isBlacklisted(token)).thenReturn(false);
		when(jwtTokenProvider.validateToken(token)).thenReturn(true);
		when(jwtTokenProvider.getClaims(token)).thenReturn(claims);
		when(roleHierarchy.getReachableGrantedAuthorities(any())).thenReturn(Collections.emptyList());

		StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
		accessor.addNativeHeader(HttpHeaders.AUTHORIZATION, bearerToken);
		accessor.setLeaveMutable(true);
		Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

		// when
		Message<?> result = webSocketAuthInterceptor.preSend(message, mock(MessageChannel.class));

		// then
		assertThat(result).isSameAs(message);
		UsernamePasswordAuthenticationToken authentication =
			(UsernamePasswordAuthenticationToken) accessor.getUser();
		assertThat(authentication.getPrincipal()).isInstanceOf(UserPrincipal.class);
		assertThat(((UserPrincipal) authentication.getPrincipal()).getId()).isEqualTo(userId);
		assertThat(((UserPrincipal) authentication.getPrincipal()).getRole()).isEqualTo(Role.USER);
	}

	@Test
	@DisplayName("preSend - 블랙리스트 토큰 예외 발생")
	void preSend_blacklist_token_exception() {
		//given
		String token = "blacklist-token";
		String bearerToken = "Bearer " + token;
		when(jwtTokenProvider.isBlacklisted(token)).thenReturn(true);

		StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
		accessor.addNativeHeader(HttpHeaders.AUTHORIZATION, bearerToken);
		accessor.setLeaveMutable(true);
		Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

		//when,then
		CustomException ex = catchThrowableOfType(
			() -> webSocketAuthInterceptor.preSend(message, mock(MessageChannel.class)),
			CustomException.class
		);
		assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.WEBSOCKET_INVALID_TOKEN);
		assertThat(ex.getMessage()).contains("블랙리스트");
	}

	@Test
	@DisplayName("preSend - 토큰 없는 경우 예외 발생")
	void preSend_no_token_exception() {
		//given
		StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
		accessor.setLeaveMutable(true);
		Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

		//when,then
		CustomException ex = catchThrowableOfType(
			() -> webSocketAuthInterceptor.preSend(message, mock(MessageChannel.class)),
			CustomException.class
		);
		assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.WEBSOCKET_INVALID_TOKEN);
		assertThat(ex.getMessage()).contains("accessToken");
	}

	@Test
	@DisplayName("preSend - 토큰 검증 실패시 예외 발생")
	void preSend_invalid_token_exception() {
		//given
		String token = "invalid-token";
		String bearerToken = "Bearer " + token;
		when(jwtTokenProvider.isBlacklisted(token)).thenReturn(false);
		when(jwtTokenProvider.validateToken(token)).thenReturn(false);

		StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
		accessor.addNativeHeader(HttpHeaders.AUTHORIZATION, bearerToken);
		accessor.setLeaveMutable(true);
		Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

		//when,then
		CustomException ex = catchThrowableOfType(
			() -> webSocketAuthInterceptor.preSend(message, mock(MessageChannel.class)),
			CustomException.class
		);
		assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.WEBSOCKET_INVALID_TOKEN);
		assertThat(ex.getMessage()).contains("토큰 검증");
	}

	@Test
	@DisplayName("preSend - CONNECT 명령 아닐 때 아무것도 하지 않음")
	void preSend_notConnectCommand() {
		StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
		accessor.setLeaveMutable(true);
		Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

		Message<?> result = webSocketAuthInterceptor.preSend(message, mock(MessageChannel.class));
		assertThat(result).isSameAs(message);
		assertThat(accessor.getUser()).isNull();
	}


	//resolveAccessToken
	@Test
	@DisplayName("resolveAccessToken - 여러 경로에서 토큰 추출")
	void resolveAccessToken_various_header_sources() throws Exception {
		// Authorization 헤더(Bearer)
		StompHeaderAccessor accessor1 = StompHeaderAccessor.create(StompCommand.CONNECT);
		accessor1.addNativeHeader(HttpHeaders.AUTHORIZATION, "Bearer t1");
		assertThat(resolveAccessToken(accessor1)).contains("t1");

		// access-token 헤더(Bearer)
		StompHeaderAccessor accessor2 = StompHeaderAccessor.create(StompCommand.CONNECT);
		accessor2.addNativeHeader("access-token", "Bearer t2");
		assertThat(resolveAccessToken(accessor2)).contains("t2");

		// token 헤더(Bearer)
		StompHeaderAccessor accessor3 = StompHeaderAccessor.create(StompCommand.CONNECT);
		accessor3.addNativeHeader("token", "Bearer t3");
		assertThat(resolveAccessToken(accessor3)).contains("t3");

		// token 헤더(plain)
		StompHeaderAccessor accessor4 = StompHeaderAccessor.create(StompCommand.CONNECT);
		accessor4.addNativeHeader("token", "t4");
		assertThat(resolveAccessToken(accessor4)).contains("t4");

		// 세션 속성
		StompHeaderAccessor accessor5 = StompHeaderAccessor.create(StompCommand.CONNECT);
		Map<String, Object> sessionAttributes = new HashMap<>();
		sessionAttributes.put("token", "t5");
		accessor5.setSessionAttributes(sessionAttributes);
		assertThat(resolveAccessToken(accessor5)).contains("t5");
	}


	private Optional<String> resolveAccessToken(StompHeaderAccessor accessor) throws Exception {
		var method = WebSocketAuthInterceptor.class.getDeclaredMethod("resolveAccessToken", StompHeaderAccessor.class);
		method.setAccessible(true);
		return (Optional<String>) method.invoke(webSocketAuthInterceptor, accessor);
	}
}
