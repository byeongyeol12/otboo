package com.codeit.otboo.domain.dm.redis;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Redis 채널로 메시지 publish
 * 채널명 : dm:{dmKey}
 */
@Service
public class RedisPublisher {
	private final RedisTemplate<String, String> redisTemplate;

	public RedisPublisher(@Qualifier("chatRoomRedisTemplate") RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	/**
	 * 해당 채널로 직렬화된 메시지를 발행
	 * @param channel
	 * @param message
	 */
	public void publish(String channel, String message){
		redisTemplate.convertAndSend(channel, message);
	}
}
