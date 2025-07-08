package com.codeit.otboo.domain.auth.service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class JwtBlacklistService {

	private final StringRedisTemplate redisTemplate;
	private static final String PREFIX = "blacklist:";

	public JwtBlacklistService(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public void blacklistToken(String token, long expirationMillis) {
		redisTemplate.opsForValue().set(PREFIX + token, "blacklisted", expirationMillis, TimeUnit.MILLISECONDS);
	}

	public boolean isBlacklisted(String token) {
		return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + token));
	}
}
