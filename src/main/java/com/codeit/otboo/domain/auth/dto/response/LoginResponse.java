package com.codeit.otboo.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "로그인 성공 시 반환되는 토큰 및 만료 정보 DTO")
public class LoginResponse {

	@Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
	private final String accessToken;

	@Schema(description = "JWT 리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
	private final String refreshToken;

	@Schema(description = "토큰 타입(Bearer 고정)", example = "Bearer")
	private final String tokenType;

	@Schema(description = "액세스 토큰 만료 시각(UTC ISO8601)", example = "2024-08-01T10:20:30Z")
	private final Instant expiresAt;

	// 풀 파라미터 생성자
	public LoginResponse(String accessToken, String refreshToken, String tokenType, Instant expiresAt) {
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.tokenType = tokenType;
		this.expiresAt = expiresAt;
	}

	// 기존에 사용하던 생성자 (예: 로그인 응답 시 기본 타입 사용)
	public LoginResponse(String accessToken, String refreshToken, Instant expiresAt) {
		this(accessToken, refreshToken, "Bearer", expiresAt);
	}

	public String getAccessToken() {
		return accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public String getTokenType() {
		return tokenType;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}
}
