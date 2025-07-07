package com.codeit.otboo.domain.auth.dto.response;

import java.time.Instant;

public class LoginResponse {

	private final String accessToken;
	private final String refreshToken;
	private final String tokenType;
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
