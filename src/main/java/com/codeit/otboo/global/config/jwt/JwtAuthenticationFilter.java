package com.codeit.otboo.global.config.jwt;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.config.security.UserPrincipal;
import com.codeit.otboo.global.enumType.Role;
import com.codeit.otboo.global.error.ErrorCode;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {

		System.out.println("🔍 [JWT 필터] 요청 URI: " + request.getRequestURI());

		String token = null;

		// ✅ 1. Authorization 헤더에서 토큰 추출
		String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			token = authorizationHeader.substring(7);
			System.out.println("✅ Authorization 헤더에서 토큰 추출 완료");
		} else {
			System.out.println("⚠️ Authorization 헤더 없음 또는 Bearer 형식 아님");
		}

		// ✅ 2. Cookie 에서 accessToken 추출
		if (token == null && request.getCookies() != null) {
			for (var cookie : request.getCookies()) {
				if ("accessToken".equals(cookie.getName())) {
					token = cookie.getValue();
					System.out.println("✅ Cookie에서 accessToken 추출 완료");
					break;
				}
			}
		}

		// ✅ 3. 토큰이 없으면 필터 통과
		if (token == null) {
			System.out.println("❌ 토큰 없음 → 인증 없이 필터 통과");
			filterChain.doFilter(request, response);
			return;
		}

		try {
			System.out.println("🔐 토큰 블랙리스트 체크");
			if (jwtTokenProvider.isBlacklisted(token)) {
				System.out.println("❌ 블랙리스트 토큰");
				throw new CustomException(ErrorCode.INVALID_TOKEN);
			}

			System.out.println("🔐 토큰 유효성 검증");
			jwtTokenProvider.validateToken(token);
			Claims claims = jwtTokenProvider.getClaims(token);

			String userId = claims.get("userId", String.class);
			String email = claims.get("email", String.class);
			String role = claims.get("role", String.class);
			System.out.println("✅ 토큰 Claim 파싱 성공: " + userId + ", " + email + ", " + role);

			UserPrincipal userPrincipal = new UserPrincipal(
				UUID.fromString(userId),
				email,
				null,
				Role.valueOf(role)
			);

			UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(
					userPrincipal,
					null,
					userPrincipal.getAuthorities()
				);

			authentication.setDetails(
				new WebAuthenticationDetailsSource().buildDetails(request)
			);

			SecurityContextHolder.getContext().setAuthentication(authentication);
			System.out.println("✅ SecurityContextHolder 인증 객체 등록 완료");

		} catch (CustomException e) {
			System.out.println("❌ JWT 인증 실패: " + e.getErrorCode().getCode());
			response.setStatus(e.getErrorCode().getStatus());
			response.setContentType("application/json");
			response.getWriter().write("""
				{
				  "code": "%s",
				  "message": "%s"
				}
				""".formatted(e.getErrorCode().getCode(), e.getErrorCode().getMessage()));
			return;
		}

		filterChain.doFilter(request, response);
	}
}