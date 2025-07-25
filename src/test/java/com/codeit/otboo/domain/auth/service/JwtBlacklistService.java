package com.codeit.otboo.domain.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class JwtBlacklistServiceTest {

	@Mock
	private StringRedisTemplate redisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations;

	private JwtBlacklistService jwtBlacklistService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		jwtBlacklistService = new JwtBlacklistService(redisTemplate);
	}

	@Test
	@DisplayName("토큰 블랙리스트 등록 성공")
	void blacklistToken_success() {
		String token = "access-token";
		long expirationMillis = 60000L;

		given(redisTemplate.opsForValue()).willReturn(valueOperations);

		jwtBlacklistService.blacklistToken(token, expirationMillis);

		verify(valueOperations).set("blacklist:" + token, "blacklisted", expirationMillis, TimeUnit.MILLISECONDS);
	}

	@Test
	@DisplayName("토큰이 블랙리스트에 등록되어 있음")
	void isBlacklisted_true() {
		String token = "access-token";
		given(redisTemplate.hasKey("blacklist:" + token)).willReturn(true);

		boolean result = jwtBlacklistService.isBlacklisted(token);
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("토큰이 블랙리스트에 등록되어 있지 않음")
	void isBlacklisted_false() {
		String token = "access-token";
		given(redisTemplate.hasKey("blacklist:" + token)).willReturn(false);

		boolean result = jwtBlacklistService.isBlacklisted(token);
		assertThat(result).isFalse();
	}
}
