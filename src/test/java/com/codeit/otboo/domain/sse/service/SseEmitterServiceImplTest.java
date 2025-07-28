package com.codeit.otboo.domain.sse.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.codeit.otboo.domain.sse.repository.SseEmitterRepository;
import com.codeit.otboo.domain.sse.repository.SseMessageRepository;
import com.codeit.otboo.domain.sse.util.SseMessage;

@ExtendWith(MockitoExtension.class)
public class SseEmitterServiceImplTest {

	@Mock
	private SseEmitterRepository sseEmitterRepository;
	@Mock
	private SseMessageRepository sseMessageRepository;

	@InjectMocks
	private SseEmitterServiceImpl sseEmitterService;


	//subscribe
	@Test
	@DisplayName("subscribe - 구독 성공(lastEventId = null)")
	void subscribe_success_with_noLastEventId() {
		// given
		UUID receiverId = UUID.randomUUID();
		UUID lastEventId = null;

		// when
		SseEmitter emitter = sseEmitterService.subscribe(receiverId, lastEventId);

		// then
		assertThat(emitter).isNotNull();
		verify(sseEmitterRepository, times(1)).save(eq(receiverId), any(SseEmitter.class));
	}

	@Test
	@DisplayName("subscribe - 구독 및 미수신 알림 복구 성공(lastEventId 존재하는 경우) ")
	void subscribe_success_withLastEventId() throws IOException {
		// given
		UUID receiverId = UUID.randomUUID();
		UUID lastEventId = UUID.randomUUID();

		SseMessage missed = SseMessage.create(receiverId, "test", "data");
		List<SseMessage> missedList = List.of(missed);

		when(sseMessageRepository.findAllByEventIdAfterAndReceiverId(lastEventId, receiverId)).thenReturn(missedList);

		SseEmitter spyEmitter = spy(new SseEmitter(300000L));
		doReturn(spyEmitter).when(sseEmitterRepository).save(eq(receiverId), any(SseEmitter.class));

		// when
		SseEmitter emitter = sseEmitterService.subscribe(receiverId, lastEventId);

		// then
		verify(sseMessageRepository, times(1)).findAllByEventIdAfterAndReceiverId(lastEventId, receiverId);
	}

	@Test
	@DisplayName("send - 특정 유저에게 메시지 전송 (성공)")
	void send_success_toUser() throws IOException {
		// given
		UUID receiverId = UUID.randomUUID();
		String eventName = "notifications";
		String data = "testData";

		SseEmitter mockEmitter = mock(SseEmitter.class);
		List<SseEmitter> emitterList = List.of(mockEmitter);

		when(sseEmitterRepository.findByReceiverId(receiverId)).thenReturn(Optional.of(emitterList));
		when(sseMessageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		// when
		sseEmitterService.send(receiverId, eventName, data);

		// then
		verify(mockEmitter, times(1)).send(anySet());
		verify(sseMessageRepository, times(1)).save(any());
	}

	@Test
	@DisplayName("send - 특정 유저에게 메시지 전송 실패(IOException 발생)")
	void send_failed_toUser() throws IOException {
		// given
		UUID receiverId = UUID.randomUUID();
		String eventName = "notifications";
		String data = "testData";

		SseEmitter mockEmitter = mock(SseEmitter.class);
		List<SseEmitter> emitterList = List.of(mockEmitter);

		when(sseEmitterRepository.findByReceiverId(receiverId)).thenReturn(Optional.of(emitterList));
		when(sseMessageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		doThrow(new IOException("fail")).when(mockEmitter).send(anySet());

		// when
		sseEmitterService.send(receiverId, eventName, data);

		// then
		verify(mockEmitter, times(1)).send(anySet());
	}

	@Test
	@DisplayName("send(SseMessage) - 모든 emitter 에게 전파")
	void send_success_toAllEmitters() throws IOException {
		// given
		SseMessage msg = SseMessage.create(UUID.randomUUID(), "event", "data");
		msg.setBroadcast(true); //브로드캐스트용으로 재설정

		SseEmitter emitter1 = mock(SseEmitter.class);
		SseEmitter emitter2 = mock(SseEmitter.class);
		when(sseEmitterRepository.findAll()).thenReturn(List.of(emitter1, emitter2));

		// when
		sseEmitterService.send(msg);

		// then
		verify(emitter1, times(1)).send(anySet());
		verify(emitter2, times(1)).send(anySet());
	}

	@Test
	@DisplayName("send(SseMessage) - 모든 emitter 에게 전파 실패 (IOException 발생)")
	void send_failed_toAllEmitters() throws IOException {
		// given
		SseMessage msg = SseMessage.create(UUID.randomUUID(), "event", "data");
		msg.setBroadcast(true);

		SseEmitter emitter1 = mock(SseEmitter.class);
		SseEmitter emitter2 = mock(SseEmitter.class);
		when(sseEmitterRepository.findAll()).thenReturn(List.of(emitter1, emitter2));

		doThrow(new IOException("fail")).when(emitter1).send(anySet());

		// when
		sseEmitterService.send(msg);

		// then
		verify(emitter1, times(1)).send(anySet());
		verify(emitter2, times(1)).send(anySet());
	}

	@Test
	@DisplayName("send(SseMessage) - 개별 유저 대상 전송")
	void send_success_toSpecificUsers() throws IOException {
		// given
		Set<UUID> receivers = new HashSet<>(List.of(UUID.randomUUID(), UUID.randomUUID()));
		SseMessage msg = makeSseMessageForTest(receivers, false, "event", "data");

		SseEmitter emitter1 = mock(SseEmitter.class);
		SseEmitter emitter2 = mock(SseEmitter.class);

		when(sseEmitterRepository.findAllByReceiverIdsIn(receivers)).thenReturn(List.of(emitter1, emitter2));

		// when
		sseEmitterService.send(msg);

		// then
		verify(emitter1, times(1)).send(anySet());
		verify(emitter2, times(1)).send(anySet());
	}
	@Test
	@DisplayName("send(SseMessage) - broadcast에서 IOException 발생 시 예외처리")
	void send_broadcast_ioexception() throws IOException {
		// given
		SseMessage msg = SseMessage.create(UUID.randomUUID(), "event", "data");
		msg.setBroadcast(true);

		SseEmitter emitter = mock(SseEmitter.class);
		when(sseEmitterRepository.findAll()).thenReturn(List.of(emitter));
		doThrow(new IOException("fail")).when(emitter).send(anySet());

		// when
		sseEmitterService.send(msg);

		// then
		verify(emitter, times(1)).send(anySet());
	}

	@Test
	@DisplayName("send(SseMessage) - 개별 전송에서 IOException 발생 시 예외처리")
	void send_individual_ioexception() throws IOException {
		// given
		Set<UUID> receivers = Set.of(UUID.randomUUID());
		SseMessage msg = makeSseMessageForTest(receivers, false, "event", "data");

		SseEmitter emitter = mock(SseEmitter.class);
		when(sseEmitterRepository.findAllByReceiverIdsIn(receivers)).thenReturn(List.of(emitter));
		doThrow(new IOException("fail")).when(emitter).send(anySet());

		// when
		sseEmitterService.send(msg);

		// then
		verify(emitter, times(1)).send(anySet());
	}

	@Test
	@DisplayName("ping - 모든 emitter에게 ping 이벤트 전송")
	void ping_success_toAllEmitters() throws IOException {
		// given
		SseEmitter emitter1 = mock(SseEmitter.class);
		SseEmitter emitter2 = mock(SseEmitter.class);
		when(sseEmitterRepository.findAll()).thenReturn(List.of(emitter1, emitter2));

		// when
		sseEmitterService.ping();

		// then
		verify(emitter1, times(1)).send(anySet());
		verify(emitter2, times(1)).send(anySet());
	}

	@Test
	@DisplayName("ping - send 실패 시 completeWithError 호출")
	void ping_failed_toAllEmitters() throws IOException {
		// given
		SseEmitter emitter = mock(SseEmitter.class);
		when(sseEmitterRepository.findAll()).thenReturn(List.of(emitter));
		doThrow(new IOException("fail")).when(emitter).send(anySet());

		// when
		sseEmitterService.ping();

		// then
		verify(emitter, times(1)).completeWithError(any(IOException.class));
	}

	//리플렉션을 이용해 테스트에서만 SseMessage 객체 생성
	private static SseMessage makeSseMessageForTest(Set<UUID> receivers, boolean broadcast, String eventName, Object data) {
		try {
			Constructor<SseMessage> constructor = SseMessage.class.getDeclaredConstructor(
				UUID.class, Set.class, boolean.class, String.class, Object.class
			);
			constructor.setAccessible(true);
			return constructor.newInstance(UUID.randomUUID(), receivers, broadcast, eventName, data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
