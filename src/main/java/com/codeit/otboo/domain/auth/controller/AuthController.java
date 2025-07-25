package com.codeit.otboo.domain.auth.controller;

import com.codeit.otboo.domain.auth.dto.request.LoginRequest;
import com.codeit.otboo.domain.auth.dto.response.AccessTokenResponse;
import com.codeit.otboo.domain.auth.dto.response.CsrfTokenResponse;
import com.codeit.otboo.domain.auth.dto.response.LoginResponse;
import com.codeit.otboo.domain.auth.service.AuthService;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.config.jwt.JwtTokenProvider;
import com.codeit.otboo.global.error.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증", description = "로그인, 로그아웃, 토큰 재발급 등 인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final JwtTokenProvider jwtTokenProvider;

	@Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 Access Token과 Refresh Token(HttpOnly Cookie)을 발급받습니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "로그인 성공"),
			@ApiResponse(responseCode = "401", description = "인증 실패 (이메일 또는 비밀번호 불일치)")
	})
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

	@Operation(summary = "로그아웃", description = "현재 사용자를 로그아웃 처리하고 사용된 토큰을 블랙리스트에 등록합니다.")
	@ApiResponse(responseCode = "200", description = "로그아웃 성공")
	@PostMapping("/sign-out")
	public ResponseEntity<String> logout(@Parameter(hidden = true) HttpServletRequest request) {
		String token = jwtTokenProvider.resolveToken(request);
		authService.logout(token);
		return ResponseEntity.ok("로그아웃 성공");
	}

	@Operation(summary = "Access Token 재발급 (GET /me)", description = "[사용되지 않음] POST /refresh를 사용해주세요.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
			@ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
	})
	@GetMapping("/me")
	public ResponseEntity<AccessTokenResponse> refresh(@Parameter(hidden = true) HttpServletRequest request) {
		String refreshToken = extractRefreshTokenFromCookie(request);
		String newAccessToken = authService.refreshAccessToken(refreshToken);
		return ResponseEntity.ok(new AccessTokenResponse(newAccessToken));
	}

	@Operation(summary = "Access Token 재발급", description = "HttpOnly 쿠키의 Refresh Token을 사용하여 새로운 Access Token을 재발급받습니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
			@ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
	})
	@PostMapping("/refresh")
	public ResponseEntity<AccessTokenResponse> refreshAccessToken(@Parameter(hidden = true) HttpServletRequest request) {
		String refreshToken = extractRefreshTokenFromCookie(request);
		String newAccessToken = authService.refreshAccessToken(refreshToken);
		return ResponseEntity.ok(new AccessTokenResponse(newAccessToken));
	}

	@Operation(summary = "CSRF 토큰 조회", description = "CSRF 보호를 위한 토큰을 조회합니다. (주로 세션 기반 로그인 시 사용)")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@GetMapping("/csrf-token")
	public CsrfTokenResponse getCsrfToken(@Parameter(hidden = true) CsrfToken csrfToken) {
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