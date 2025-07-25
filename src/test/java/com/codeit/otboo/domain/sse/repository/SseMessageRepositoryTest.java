package com.codeit.otboo.domain.sse.repository;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.codeit.otboo.domain.sse.util.SseMessage;

public class SseMessageRepositoryTest {
	private SseMessageRepository sseMessageRepository;

	@BeforeEach
	void setUp() {
		sseMessageRepository = new SseMessageRepository();
		setField(sseMessageRepository, "eventQueueCapacity", 3);
	}
	//private 값 강제 변경
	private void setField(Object target, String fieldName, Object value) {
		try {
			Field field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	// 내부 eventIdQueue 필드 접근
	private List<UUID> getEventIdQueue() {
		try {
			Field field = sseMessageRepository.getClass().getDeclaredField("eventIdQueue");
			field.setAccessible(true);
			ConcurrentLinkedDeque<UUID> queue = (ConcurrentLinkedDeque<UUID>) field.get(sseMessageRepository);
			return new ArrayList<>(queue);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	// 내부 messages 필드 접근
	private Map<UUID, SseMessage> getMessages() {
		try {
			Field field = sseMessageRepository.getClass().getDeclaredField("messages");
			field.setAccessible(true);
			Map<UUID, SseMessage> messages = (Map<UUID, SseMessage>) field.get(sseMessageRepository);
			return messages;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	//save
	@Test
	@DisplayName("save - 메시지 저장 성공(eventIdQueue,message map)")
	void save_success() {
		//given
		UUID receiverId = UUID.randomUUID();
		String eventName = "test";
		Object eventData = "test-data";
		SseMessage message = SseMessage.create(receiverId, eventName, eventData);

		//when
		SseMessage result = sseMessageRepository.save(message);

		// then
		assertThat(result).isEqualTo(message); // 리턴값 = 입력값
		List<UUID> eventIds = getEventIdQueue();
		Map<UUID, SseMessage> messagesMap = getMessages();
		assertThat(eventIds).containsExactly(message.getEventId()); // eventIdQueue 추가
		assertThat(messagesMap.get(message.getEventId())).isEqualTo(message); // messages map 저장
	}

	@Test
	@DisplayName("save - 용량 초과시 오래된 메시지부터 삭제")
	void save_overLimit(){
		// given
		UUID receiverId = UUID.randomUUID();
		List<SseMessage> messages = new ArrayList<>();

		for (int i = 0; i < 5; i++) {
			SseMessage msg = SseMessage.create(receiverId, "test" + i, "test-data" + i);
			messages.add(sseMessageRepository.save(msg));
		}

		// when,then
		List<UUID> eventIds = getEventIdQueue();
		Map<UUID, SseMessage> messagesMap = getMessages();

		assertThat(eventIds).hasSize(3);
		assertThat(eventIds).containsExactly(
			messages.get(2).getEventId(),
			messages.get(3).getEventId(),
			messages.get(4).getEventId()
		);

		assertThat(messagesMap.keySet()).containsExactlyInAnyOrder(
			messages.get(2).getEventId(),
			messages.get(3).getEventId(),
			messages.get(4).getEventId()
		);
	}

	//findAllByEventIdAfterAndReceiverId
	@Test
	@DisplayName("findAllByEventIdAfterAndReceiverId - 특정 이벤트ID 이후, 특정 수신자에게 도달하는 메시지만 필터링해 반환")
	void findAllByEventIdAfterAndReceiverId_filter() {
		// given
		UUID receiverId1 = UUID.randomUUID();
		UUID receiverId2 = UUID.randomUUID();

		SseMessage msg1 = SseMessage.create(receiverId1, "test", "A");
		SseMessage msg2 = SseMessage.create(receiverId2, "test", "B");
		SseMessage msg3 = SseMessage.create(receiverId1, "test", "C");

		sseMessageRepository.save(msg1);
		sseMessageRepository.save(msg2);
		sseMessageRepository.save(msg3);

		// when
		List<SseMessage> result = sseMessageRepository.findAllByEventIdAfterAndReceiverId(msg2.getEventId(), receiverId1);

		// then
		assertThat(result).hasSize(1).allMatch(msg->msg.getEventData().equals("C"));
	}

	@Test
	@DisplayName("findAllByEventIdAfterAndReceiverId - 이벤트ID 이후가 없으면 빈 리스트 반환")
	void findAllByEventIdAfterAndReceiverId_noId_returnEmpty() {
		// given
		UUID receiverId = UUID.randomUUID();
		SseMessage msg = SseMessage.create(receiverId, "test", "A");
		sseMessageRepository.save(msg);

		//when
		List<SseMessage> result = sseMessageRepository.findAllByEventIdAfterAndReceiverId(UUID.randomUUID(), receiverId);

		// then
		assertThat(result).isEmpty();
	}
}
