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

/**
 * 소켓 연결 시 STOMP CONNECT 명령에서 토큰 검사
 */
@Slf4j
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {
	private final JwtTokenProvider jwtTokenProvider;
	private final RoleHierarchy roleHierarchy;

	/**
	 * CONNECT 명령일 때 토큰 추출 -> 블랙리스트/유효성/권한 체크
	 * 인증 성공 시 -> accessor.setUser() 로 인증객체(Principa) 세팅 -> 이후 컨트롤러에서 @AuthenticationPrincipal 사용 가능
	 */
	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

		log.info("[WebSocketAuthInterceptor] preSend 진입: {}", accessor != null ? accessor.getCommand() : "Accessor null");

		if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
			// 1. 전체 헤더 로그
			log.info("[WebSocketAuthInterceptor] CONNECT NativeHeaders: {}", accessor.toNativeHeaderMap()); // 클라이언트가 보낸 header 확인
			// 2. AccessToken 추출 시도
			String accessToken = resolveAccessToken(accessor).orElse(null);
			log.info("[WebSocketAuthInterceptor] AccessToken 추출 결과: {}", accessToken);

			// 3. 블랙리스트 체크
			if (accessToken == null) {
				log.warn("[WebSocketAuthInterceptor] 토큰이 없어 연결 거부");
				throw new CustomException(ErrorCode.WEBSOCKET_INVALID_TOKEN, "accessToken 이 없습니다.");
			}
			if (jwtTokenProvider.isBlacklisted(accessToken)) {
				log.warn("[WebSocketAuthInterceptor] 블랙리스트 토큰 거부: {}", accessToken);
				throw new CustomException(ErrorCode.WEBSOCKET_INVALID_TOKEN, "블랙리스트 체크를 통과하지 못했습니다.");
			}

			// 4. 토큰 검증
			if (jwtTokenProvider.validateToken(accessToken)) {
				log.info("[WebSocketAuthInterceptor] 토큰 검증 성공");
				Claims claims = jwtTokenProvider.getClaims(accessToken);
				UUID userId = UUID.fromString(claims.get("userId", String.class));
				String email = claims.get("email", String.class);
				String roleStr = claims.get("role", String.class);
				Role role = Role.valueOf(roleStr); // enum 변환

				log.info("[WebSocketAuthInterceptor] 인증 성공: userId={}, email={}, role={}", userId, email, role);
				// 인증 객체 생성 및 세션에 심기
				UserPrincipal userPrincipal = new UserPrincipal(userId, email, null, role);
				UsernamePasswordAuthenticationToken authenticationToken =
					new UsernamePasswordAuthenticationToken(
						userPrincipal,
						null,
						roleHierarchy.getReachableGrantedAuthorities(userPrincipal.getAuthorities())
					);
				accessor.setUser(authenticationToken);
				log.info("[WebSocketAuthInterceptor] setUser()로 인증 객체 등록 완료: authorities={}", authenticationToken.getAuthorities());
			} else {
				log.warn("[WebSocketAuthInterceptor] 토큰 검증 실패");
				throw new CustomException(ErrorCode.WEBSOCKET_INVALID_TOKEN, "토큰 검증에 실패했습니다.");
			}
		}
		return message;
	}

	/**
	 * 헤더/쿼리/세션 등 다양한 경로에서 토큰 추출
	 *
	 */
	private Optional<String> resolveAccessToken(StompHeaderAccessor accessor) {
		String prefix = "Bearer ";

		// 전체 Native Headers 로그
		log.info("[resolveAccessToken] CONNECT NativeHeaders: {}", accessor.toNativeHeaderMap());

		// 표준 Authorization 헤더
		String token = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
		log.info("[resolveAccessToken] Authorization 헤더: {}", token);
		if (token != null && token.startsWith(prefix)) {
			log.info("[resolveAccessToken] Authorization에서 토큰 추출 성공");
			return Optional.of(token.substring(prefix.length()));
		}

		// 커스텀 access-token 헤더
		token = accessor.getFirstNativeHeader("access-token");
		log.info("[resolveAccessTokenh] access-token 헤더: {}", token);
		if (token != null && token.startsWith(prefix)) {
			log.info("[resolveAccessToken] access-token에서 토큰 추출 성공");
			return Optional.of(token.substring(prefix.length()));
		}

		// 쿼리파라미터 token 지원
		token = accessor.getFirstNativeHeader("token");
		log.info("[resolveAccessToken] token 헤더/파라미터: {}", token);
		if (token != null) {
			if (token.startsWith(prefix)) {
				log.info("[resolveAccessToken] token(Bearer)에서 토큰 추출 성공");
				return Optional.of(token.substring(prefix.length()));
			}
			log.info("[resolveAccessToken] token(Plain)에서 토큰 추출 성공");
			return Optional.of(token);
		}

		// 세션 속성 등에서 token 시도
		Object sessionToken = accessor.getSessionAttributes() != null
			? accessor.getSessionAttributes().get("token")
			: null;
		log.info("[resolveAccessToken] sessionAttributes token: {}", sessionToken);
		if (sessionToken != null) {
			token = sessionToken.toString();
			if (token.startsWith(prefix)) {
				log.info("[resolveAccessToken] sessionAttributes(Bearer)에서 토큰 추출 성공");
				return Optional.of(token.substring(prefix.length()));
			}
			log.info("[resolveAccessToken] sessionAttributes(Plain)에서 토큰 추출 성공");
			return Optional.of(token);
		}

		// 아무 토큰도 못 찾았을 때
		log.warn("[resolveAccessToken] 토큰 추출 실패: 어떠한 헤더/파라미터/세션에서도 토큰을 찾지 못함!");
		return Optional.empty();
	}
}
