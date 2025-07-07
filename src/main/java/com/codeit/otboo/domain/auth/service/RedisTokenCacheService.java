package com.codeit.otboo.domain.auth.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisTokenCacheService implements TokenCacheService {

	private static final String KEY_PREFIX = "refresh:";
	private static final Duration TTL = Duration.ofDays(7);

	private final StringRedisTemplate redisTemplate;

	public RedisTokenCacheService(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	public void saveRefreshToken(UUID userId, String refreshToken) {
		String key = getKey(userId);
		redisTemplate.opsForValue().set(key, refreshToken, TTL);
	}

	@Override
	public void invalidateRefreshToken(UUID userId) {
		redisTemplate.delete(getKey(userId));
	}

	@Override
	public boolean isRefreshTokenValid(UUID userId, String refreshToken) {
		String storedToken = redisTemplate.opsForValue().get(getKey(userId));
		return refreshToken.equals(storedToken);
	}

	private String getKey(UUID userId) {
		return KEY_PREFIX + userId.toString();
	}
}
