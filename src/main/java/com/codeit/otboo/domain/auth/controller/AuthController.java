package com.codeit.otboo.domain.auth.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.otboo.domain.auth.dto.request.LoginRequest;
import com.codeit.otboo.domain.auth.dto.response.AccessTokenResponse;
import com.codeit.otboo.domain.auth.dto.response.CsrfTokenResponse;
import com.codeit.otboo.domain.auth.dto.response.LoginResponse;
import com.codeit.otboo.domain.auth.service.AuthService;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.config.jwt.JwtTokenProvider;
import com.codeit.otboo.global.error.ErrorCode;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final JwtTokenProvider jwtTokenProvider;

	@PostMapping("/sign-in")
	public ResponseEntity<String> signIn(@RequestBody @Valid LoginRequest request) {
		LoginResponse response = authService.login(request);

		ResponseCookie cookie = ResponseCookie.from("refreshToken", response.getRefreshToken())
			.httpOnly(true)
			.secure(false) // 개발 중이므로 false, 운영 시 true 권장
			.path("/")
			.maxAge(7 * 24 * 60 * 60)
			.sameSite("Lax")
			.build();

		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, cookie.toString())
			.body(response.getAccessToken());
	}

	@PostMapping("/sign-out")
	public ResponseEntity<String> logout(HttpServletRequest request) {
		String token = jwtTokenProvider.resolveToken(request);
		authService.logout(token);
		return ResponseEntity.ok("로그아웃 성공");
	}

	@GetMapping("/me")
	public ResponseEntity<AccessTokenResponse> refresh(HttpServletRequest request) {
		String refreshToken = extractRefreshTokenFromCookie(request);
		String newAccessToken = authService.refreshAccessToken(refreshToken);
		return ResponseEntity.ok(new AccessTokenResponse(newAccessToken));
	}

	@PostMapping("/refresh")
	public ResponseEntity<AccessTokenResponse> refreshAccessToken(HttpServletRequest request) {
		String refreshToken = extractRefreshTokenFromCookie(request);
		String newAccessToken = authService.refreshAccessToken(refreshToken);
		return ResponseEntity.ok(new AccessTokenResponse(newAccessToken));
	}

	@GetMapping("/csrf-token")
	public CsrfTokenResponse getCsrfToken(CsrfToken csrfToken) {
		return new CsrfTokenResponse(
			csrfToken.getHeaderName(),
			csrfToken.getParameterName(),
			csrfToken.getToken()
		);
	}

	private String extractRefreshTokenFromCookie(HttpServletRequest request) {
		if (request.getCookies() == null) {
			throw new CustomException(ErrorCode.UNAUTHORIZED);
		}

		for (Cookie cookie : request.getCookies()) {
			if ("refreshToken".equals(cookie.getName())) {
				return cookie.getValue();
			}
		}

		throw new CustomException(ErrorCode.UNAUTHORIZED);
	}

}
