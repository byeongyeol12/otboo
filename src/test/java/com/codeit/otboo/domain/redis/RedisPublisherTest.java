package com.codeit.otboo.domain.redis;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import com.codeit.otboo.domain.dm.redis.RedisPublisher;

@ExtendWith(MockitoExtension.class)
public class RedisPublisherTest {

	@Test
	@DisplayName("publish - 지정한 채널로 메시지 발행")
	void publish_success() {
		//given
		RedisTemplate<String,String> redisTemplate = mock(RedisTemplate.class);
		RedisPublisher redisPublisher = new RedisPublisher(redisTemplate);

		String channel = "test-channel";
		String message = "test-message";

		//when
		redisPublisher.publish(channel, message);

		//then
		verify(redisTemplate,times(1))
			.convertAndSend(eq(channel), eq(message));
	}

	@Test
	@DisplayName("publish - null 채널, 메시지 허용 여부")
	void publish_null() {
		//given
		RedisTemplate<String,String> redisTemplate = mock(RedisTemplate.class);
		RedisPublisher redisPublisher = new RedisPublisher(redisTemplate);

		//when
		redisPublisher.publish(null,null);

		//then
		verify(redisTemplate).convertAndSend(isNull(),isNull());
	}
}
