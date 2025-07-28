package com.codeit.otboo.domain.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisTokenCacheServiceTest {

	@Mock
	private StringRedisTemplate redisTemplate;

	private RedisTokenCacheService tokenCacheService;

	@BeforeEach
	void setup() {
		tokenCacheService = new RedisTokenCacheService(redisTemplate);
	}

	@Test
	@DisplayName("리프레시 토큰 저장")
	void saveRefreshToken_success() {
		UUID userId = UUID.randomUUID();
		String token = "refresh-token";
		ValueOperations<String, String> ops = mock(ValueOperations.class);

		given(redisTemplate.opsForValue()).willReturn(ops);

		tokenCacheService.saveRefreshToken(userId, token);

		verify(ops).set(
			"refresh:" + userId.toString(),
			token,
			Duration.ofDays(7)
		);
	}

	@Test
	@DisplayName("리프레시 토큰 삭제")
	void invalidateRefreshToken_success() {
		UUID userId = UUID.randomUUID();

		tokenCacheService.invalidateRefreshToken(userId);

		verify(redisTemplate).delete("refresh:" + userId.toString());
	}

	@Test
	@DisplayName("리프레시 토큰 유효성 검증 - 일치")
	void isRefreshTokenValid_match() {
		UUID userId = UUID.randomUUID();
		String token = "refresh-token";
		ValueOperations<String, String> ops = mock(ValueOperations.class);

		given(redisTemplate.opsForValue()).willReturn(ops);
		given(ops.get("refresh:" + userId.toString())).willReturn("refresh-token");

		assertThat(tokenCacheService.isRefreshTokenValid(userId, token)).isTrue();
	}

	@Test
	@DisplayName("리프레시 토큰 유효성 검증 - 불일치")
	void isRefreshTokenValid_mismatch() {
		UUID userId = UUID.randomUUID();
		String token = "invalid-token";
		ValueOperations<String, String> ops = mock(ValueOperations.class);

		given(redisTemplate.opsForValue()).willReturn(ops);
		given(ops.get("refresh:" + userId.toString())).willReturn("refresh-token");

		assertThat(tokenCacheService.isRefreshTokenValid(userId, token)).isFalse();
	}
}

