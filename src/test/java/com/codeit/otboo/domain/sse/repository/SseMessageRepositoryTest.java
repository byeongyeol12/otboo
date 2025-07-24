// package com.codeit.otboo.domain.sse.repository;
//
// import java.lang.reflect.Field;
// import java.util.UUID;
//
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
//
// import com.codeit.otboo.domain.sse.util.SseMessage;
//
// public class SseMessageRepositoryTest {
// 	private SseMessageRepository sseMessageRepository;
//
// 	private int defaultCapacity = 3;
//
// 	@BeforeEach
// 	void setUp() throws IllegalAccessException, NoSuchFieldException {
// 		sseMessageRepository = new SseMessageRepository();
// 		Field field = SseMessageRepository.class.getDeclaredField("eventQueueCapacity");
// 		field.setAccessible(true);
// 		field.set(sseMessageRepository,defaultCapacity);
// 	}
//
// 	@Test
// 	@DisplayName("save - 메시지 저장 성공")
// 	void save_success() {
// 		//given
// 		UUID eventId = UUID.randomUUID();
// 		UUID receiverId = UUID.randomUUID();
// 		SseMessage sseMessage = SseMessage.create(receiverId,"test","test data");
//
// 	}
// }
