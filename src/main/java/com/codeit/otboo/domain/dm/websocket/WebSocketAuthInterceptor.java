package com.codeit.otboo.domain.dm.websocket;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.config.jwt.JwtTokenProvider;
import com.codeit.otboo.global.config.security.UserPrincipal;
import com.codeit.otboo.global.enumType.Role;
import com.codeit.otboo.global.error.ErrorCode;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {
	private final JwtTokenProvider jwtTokenProvider;
	private final RoleHierarchy roleHierarchy;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		log.info("[WS-INTCP] preSend 진입: {}", accessor != null ? accessor.getCommand() : "Accessor null");
		if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
			// 1. 전체 헤더 로그
			log.info("[WS-INTCP] CONNECT NativeHeaders: {}", accessor.toNativeHeaderMap());
			// 2. AccessToken 추출 시도
			String accessToken = resolveAccessToken(accessor).orElse(null);
			log.info("[WS-INTCP] AccessToken 추출 결과: {}", accessToken);

			// 3. 블랙리스트 체크
			if (accessToken == null) {
				log.warn("[WS-INTCP] 토큰이 없어 연결 거부");
				throw new CustomException(ErrorCode.WEBSOCKET_INVALID_TOKEN, "No token");
			}
			if (jwtTokenProvider.isBlacklisted(accessToken)) {
				log.warn("[WS-INTCP] 블랙리스트 토큰 거부: {}", accessToken);
				throw new CustomException(ErrorCode.WEBSOCKET_INVALID_TOKEN, "블랙리스트 체크를 통과하지 못했습니다.");
			}
			// 4. 토큰 검증
			if (jwtTokenProvider.validateToken(accessToken)) {
				log.info("[WS-INTCP] 토큰 검증 성공");
				Claims claims = jwtTokenProvider.getClaims(accessToken);
				UUID userId = UUID.fromString(claims.get("userId", String.class));
				String email = claims.get("email", String.class);
				String roleStr = claims.get("role", String.class);
				Role role = Role.valueOf(roleStr); // enum 변환

				log.info("[WS-INTCP] 인증 성공: userId={}, email={}, role={}", userId, email, role);
				// 인증 객체 생성 및 세션에 심기
				UserPrincipal userPrincipal = new UserPrincipal(userId, email, null, role);
				UsernamePasswordAuthenticationToken authenticationToken =
					new UsernamePasswordAuthenticationToken(
						userPrincipal,
						null,
						roleHierarchy.getReachableGrantedAuthorities(userPrincipal.getAuthorities())
					);
				accessor.setUser(authenticationToken); // ⭐️ 실제로 꼭 넣어줘야 함!
				log.info("[WS-INTCP] setUser()로 인증 객체 등록 완료: authorities={}", authenticationToken.getAuthorities());
			} else {
				log.warn("[WS-INTCP] 토큰 검증 실패");
				throw new CustomException(ErrorCode.WEBSOCKET_INVALID_TOKEN, "토큰 검증 실패");
			}
		}
		return message;
	}

	private Optional<String> resolveAccessToken(StompHeaderAccessor accessor) {
		String prefix = "Bearer ";

		// 1. 전체 Native Headers 로그
		log.info("[WebSocketAuth] CONNECT NativeHeaders: {}", accessor.toNativeHeaderMap());

		// 2. 표준 Authorization 헤더
		String token = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
		log.info("[WebSocketAuth] 1. Authorization 헤더: {}", token);
		if (token != null && token.startsWith(prefix)) {
			log.info("[WebSocketAuth] 1-1. Authorization에서 토큰 추출 성공");
			return Optional.of(token.substring(prefix.length()));
		}

		// 3. 커스텀 access-token 헤더
		token = accessor.getFirstNativeHeader("access-token");
		log.info("[WebSocketAuth] 2. access-token 헤더: {}", token);
		if (token != null && token.startsWith(prefix)) {
			log.info("[WebSocketAuth] 2-1. access-token에서 토큰 추출 성공");
			return Optional.of(token.substring(prefix.length()));
		}

		// 4. 쿼리파라미터 token 지원
		token = accessor.getFirstNativeHeader("token");
		log.info("[WebSocketAuth] 3. token 헤더/파라미터: {}", token);
		if (token != null) {
			if (token.startsWith(prefix)) {
				log.info("[WebSocketAuth] 3-1. token(Bearer)에서 토큰 추출 성공");
				return Optional.of(token.substring(prefix.length()));
			}
			log.info("[WebSocketAuth] 3-2. token(Plain)에서 토큰 추출 성공");
			return Optional.of(token);
		}

		// 5. 세션 속성 등에서 token 시도
		Object sessionToken = accessor.getSessionAttributes() != null
			? accessor.getSessionAttributes().get("token")
			: null;
		log.info("[WebSocketAuth] 4. sessionAttributes token: {}", sessionToken);
		if (sessionToken != null) {
			token = sessionToken.toString();
			if (token.startsWith(prefix)) {
				log.info("[WebSocketAuth] 4-1. sessionAttributes(Bearer)에서 토큰 추출 성공");
				return Optional.of(token.substring(prefix.length()));
			}
			log.info("[WebSocketAuth] 4-2. sessionAttributes(Plain)에서 토큰 추출 성공");
			return Optional.of(token);
		}

		// 6. 아무 토큰도 못 찾았을 때
		log.warn("[WebSocketAuth] 5. 토큰 추출 실패: 어떠한 헤더/파라미터/세션에서도 토큰을 찾지 못함!");
		return Optional.empty();
	}
}
