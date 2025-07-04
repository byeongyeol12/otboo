package com.codeit.otboo.global.config.jwt;

import java.io.IOException;
import java.util.UUID;

import org.apache.http.HttpHeaders;
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

		String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			String token = authorizationHeader.substring(7);

			try {
				if (jwtTokenProvider.isBlacklisted(token)) {
					throw new CustomException(ErrorCode.INVALID_TOKEN);
				}

				jwtTokenProvider.validateToken(token);
				Claims claims = jwtTokenProvider.getClaims(token);

				String userId = claims.get("userId", String.class);
				String email = claims.get("email", String.class);
				String role = claims.get("role", String.class);

				UserPrincipal userPrincipal = new UserPrincipal(
					UUID.fromString(userId),
					email,
					null, // password not required here
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

			} catch (CustomException e) {
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

		filterChain.doFilter(request, response);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();

		return path.equals("/api/users") ||
			path.startsWith("/api/auth") ||
			path.startsWith("/h2-console");
	}
}