package com.codeit.otboo.global.config.jwt;

import java.io.IOException;
import java.util.Collections;

import org.apache.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.codeit.otboo.exception.CustomException;
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

		String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			String token = authorizationHeader.substring(7);

			try {
				if (jwtTokenProvider.isBlacklisted(token)) {
					throw new CustomException(ErrorCode.INVALID_TOKEN);
				}

				jwtTokenProvider.validateToken(token);
				Claims claims = jwtTokenProvider.getClaims(token);

				String email = claims.get("email", String.class);
				String role = claims.get("role", String.class);

				// 간단한 인증 객체 생성 (Principal: email, Authorities: ROLE_XXX)
				UsernamePasswordAuthenticationToken authentication =
					new UsernamePasswordAuthenticationToken(
						email,
						null,
						Collections.singleton(() -> "ROLE_" + role)
					);

				authentication.setDetails(
					new WebAuthenticationDetailsSource().buildDetails(request)
				);

				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (CustomException e) {
				// ❗ 필터에서 발생한 예외는 직접 응답 처리해줘야 함
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
		}

		// 다음 필터로 넘김
		filterChain.doFilter(request, response);
	}
}
