package com.codeit.otboo.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.codeit.otboo.domain.dm.redis.RedisSubscriber;

@Configuration
@EnableRedisRepositories
public class RedisConfig {

	// Redis 서버의 호스트 주소
	@Value("${spring.data.redis.host}")
	private String redisHost;

	// Redis 서버의 포트 번호
	@Value("${spring.data.redis.port}")
	private int redisPort;

	// Redis 에서 메시지를 수신하고 리스너에 전달하는 컨테이너 설정
	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer(
		RedisConnectionFactory redisConnectionFactory,
		MessageListenerAdapter listenerAdapter
	){
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(redisConnectionFactory);
		container.addMessageListener(listenerAdapter, new PatternTopic("dm:*"));
		return container;
	}

	// Redis 서버와의 연결을 설정하고 관리
	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		return new LettuceConnectionFactory(redisHost,redisPort);
	}

	// 실제 메시지를 처리하는 subscriber 설정
	@Bean
	public MessageListenerAdapter listenerAdapter(RedisSubscriber subscriber){
		return new MessageListenerAdapter(subscriber,"onMessage");
	}

	// Redis에 채팅방/메시지/기타 객체를 저장, 조회, 수정, 삭제할 때 사용
	@Bean
	public RedisTemplate<String, String> chatRoomRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, String> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory);

		// 키를 위한 직렬화
		template.setKeySerializer(new StringRedisSerializer());

		// 값을 위한 직렬화
		template.setValueSerializer(new StringRedisSerializer());

		return template;
	}
}
