package com.codeit.otboo.domain.sse.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class SseEmitterRepositoryTest {

	private SseEmitterRepository sseEmitterRepository;

	private SseEmitter emitter1, emitter2, emitter3;
	private UUID userId1,userId2,userId3;

	@BeforeEach
	void setUp() {
		sseEmitterRepository = new SseEmitterRepository();

		userId1 = UUID.randomUUID();
		userId2 = UUID.randomUUID();
		userId3 = UUID.randomUUID();

		emitter1 = new SseEmitter();
		emitter2 = new SseEmitter();
		emitter3 = new SseEmitter();
	}

	//save
	@Test
	@DisplayName("save - 동일 유저에 여러 emitter 저장")
	void save_multiEmitter_success(){
		//given
		sseEmitterRepository.save(userId1, emitter1);
		sseEmitterRepository.save(userId1, emitter2);

		//when
		Optional<List<SseEmitter>> result = sseEmitterRepository.findByReceiverId(userId1);

		//then
		assertThat(result).isPresent();
		assertThat(result.get()).containsExactly(emitter1, emitter2); //저장 순서, 객체 일치 확인
	}

	//findByReceiverId
	@Test
	@DisplayName("findByReceiverId - emitter 없으면 empty")
	void findByReceiverId_empty(){
		//given
		
		//when
		Optional<List<SseEmitter>> result = sseEmitterRepository.findByReceiverId(userId1);

		//then
		assertThat(result).isEmpty();
	}

	//findAllByReceiverIdsIn
	@Test
	@DisplayName("findAllByReceiverIdsIn - 여러 유저에 저장된 emitter 반환")
	void findByReceiverIdsIn_success(){
		//given
		sseEmitterRepository.save(userId1, emitter1);
		sseEmitterRepository.save(userId2, emitter2);
		sseEmitterRepository.save(userId2, emitter3);

		//when
		List<SseEmitter> result = sseEmitterRepository.findAllByReceiverIdsIn(List.of(userId1,userId2));

		//then
		assertThat(result).contains(emitter1, emitter2, emitter3);
	}

	//findAll
	@Test
	@DisplayName("findAll - 전체 emitter 반환")
	void findAll_success(){
		//given
		sseEmitterRepository.save(userId1, emitter1);
		sseEmitterRepository.save(userId2, emitter2);
		sseEmitterRepository.save(userId2, emitter3);

		//when
		List<SseEmitter> result = sseEmitterRepository.findAll();

		//then
		assertThat(result).containsExactlyInAnyOrder(emitter1, emitter2, emitter3);
	}

	//delete
	@Test
	@DisplayName("delete - 특정 emitter 삭제")
	void delete_success(){
		//given
		sseEmitterRepository.save(userId1, emitter1);
		sseEmitterRepository.save(userId1, emitter2);

		//when
		sseEmitterRepository.delete(userId1, emitter1);

		//then
		Optional<List<SseEmitter>> result = sseEmitterRepository.findByReceiverId(userId1);
		assertThat(result).isPresent();
		assertThat(result.get()).containsExactly(emitter2);
	}
}
