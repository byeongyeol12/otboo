package com.codeit.otboo.domain.dm.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisPublisher {
	private final RedisTemplate<String, String> redisTemplate;

	// 메시지를 Redis pub/sub 로 발행
	public void publish(String channel, String message){
		redisTemplate.convertAndSend(channel, message);
	}
}
