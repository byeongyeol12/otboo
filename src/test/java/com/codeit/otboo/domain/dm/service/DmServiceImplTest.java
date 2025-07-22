package com.codeit.otboo.domain.dm.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.codeit.otboo.domain.dm.dto.DirectMessageCreateRequest;
import com.codeit.otboo.domain.dm.dto.DirectMessageDto;
import com.codeit.otboo.domain.dm.entity.Dm;
import com.codeit.otboo.domain.dm.mapper.DirectMessageMapper;
import com.codeit.otboo.domain.dm.redis.RedisPublisher;
import com.codeit.otboo.domain.dm.repository.DmRepository;
import com.codeit.otboo.domain.dm.websocket.NewDmEvent;
import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.service.NotificationService;
import com.codeit.otboo.domain.user.dto.response.UserSummaryDto;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.enumType.Role;
import com.codeit.otboo.global.error.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
public class DmServiceImplTest {

	@InjectMocks
	private DmServiceImpl dmService;

	@Mock
	private DmRepository dmRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private NotificationService notificationService;
	@Mock
	private DirectMessageMapper directMessageMapper;
	@Mock
	private ObjectMapper objectMapper;
	@Mock
	private RedisPublisher redisPublisher;
	@Mock
	private ApplicationEventPublisher applicationEventPublisher;

	private User sender, receiver;
	private Dm dm;
	private DirectMessageDto dmDto;
	private UserSummaryDto senderSummary, receiverSummary;

	@BeforeEach
	void setUp() {
		sender = new User();
		sender.setId(UUID.randomUUID());
		sender.setName("sender");
		sender.setEmail("sender@email.com");
		sender.setPasswordHash("pw1");
		sender.setRole(Role.USER);
		sender.setField("T1");
		userRepository.save(sender);

		receiver = new User();
		receiver.setId(UUID.randomUUID());
		receiver.setName("receiver");
		receiver.setEmail("receiver@email.com");
		receiver.setPasswordHash("pw2");
		receiver.setRole(Role.USER);
		receiver.setField("T2");
		userRepository.save(receiver);

		senderSummary = new UserSummaryDto(
			sender.getId(), sender.getName(), null
		);
		receiverSummary = new UserSummaryDto(
			receiver.getId(), receiver.getName(), null
		);

		dm = new Dm(UUID.randomUUID(), sender, receiver, "content", Instant.now());
		dmDto = new DirectMessageDto(
			dm.getId(),
			Instant.now(),
			senderSummary,
			receiverSummary,
			dm.getContent()
		);
	}

	private List<Dm> makeDmList(int count) {
		List<Dm> dmList = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			dmList.add(new Dm(
				UUID.randomUUID(),sender,receiver,"msg"+i,Instant.now().plusSeconds(i)
			));
		}
		return dmList;
	}

	//sendDirectMessage
	@Test
	@DisplayName("sendDirectMessage - DM 전송 성공")
	void sendDirectMessage_succss() throws Exception {
		//given
		DirectMessageCreateRequest dmRequest = new DirectMessageCreateRequest(
			receiver.getId(), sender.getId(), "content"
		);
		when(userRepository.findById(dmRequest.senderId())).thenReturn(Optional.of(sender));
		when(userRepository.findById(dmRequest.receiverId())).thenReturn(Optional.of(receiver));
		when(dmRepository.save(any(Dm.class))).thenReturn(dm);
		when(directMessageMapper.toDirectMessageDto(any(Dm.class))).thenReturn(dmDto);
		when(objectMapper.writeValueAsString(any())).thenReturn("{test-Json}");

		//when
		DirectMessageDto result = dmService.sendDirectMessage(dmRequest);

		//then
		assertThat(result).isNotNull();
		verify(userRepository, times(1)).findById(sender.getId());
		verify(userRepository, times(1)).findById(receiver.getId());
		verify(dmRepository, times(1)).save(any(Dm.class));
		verify(redisPublisher, times(1)).publish(startsWith("dm:"), anyString());
		verify(applicationEventPublisher, times(1)).publishEvent(any(NewDmEvent.class));
		verify(notificationService, times(1)).createAndSend(any(NotificationDto.class));
	}

	@Test
	@DisplayName("sendDirectMessage - 발신자 없음으로 실패")
	void sendDirectMessage_senderNotFound() {
		//given
		DirectMessageCreateRequest dmRequest = new DirectMessageCreateRequest(
			receiver.getId(), sender.getId(), "content"
		);
		when(userRepository.findById(dmRequest.senderId())).thenReturn(Optional.empty());

		//when,then
		assertThatThrownBy(() -> dmService.sendDirectMessage(dmRequest))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining("발신자를 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("sendDirectMessage - 수신자 없음으로 실패")
	void sendDirectMessage_receiverNotFound() {
		//given
		DirectMessageCreateRequest dmRequest = new DirectMessageCreateRequest(
			receiver.getId(), sender.getId(), "content"
		);
		when(userRepository.findById(dmRequest.senderId())).thenReturn(Optional.of(sender));
		when(userRepository.findById(dmRequest.receiverId())).thenReturn(Optional.empty());

		//when,then
		assertThatThrownBy(() -> dmService.sendDirectMessage(dmRequest))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining("수신자를 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("sendDirectMessage - Redis publish 예외 발생")
	void sendDirectMessage_redisPublishExcpetion() throws Exception {
		// given
		DirectMessageCreateRequest dmRequest = new DirectMessageCreateRequest(sender.getId(), receiver.getId(),
			"hello");
		when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
		when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
		when(dmRepository.save(any(Dm.class))).thenReturn(dm);
		when(directMessageMapper.toDirectMessageDto(any(Dm.class))).thenReturn(dmDto);
		when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("Redis 실패"));

		// when, then
		assertThatThrownBy(() -> dmService.sendDirectMessage(dmRequest))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.DM_Redis_MESSAGE_ERROR);
	}

	@Test
	@DisplayName("sendDirectMessage - 알림 발송 실패")
	void sendDirectMessage_notificationFailed() throws Exception {
		// given
		DirectMessageCreateRequest dmRequest = new DirectMessageCreateRequest(sender.getId(), receiver.getId(),
			"hello");
		when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
		when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
		when(dmRepository.save(any(Dm.class))).thenReturn(dm);
		when(directMessageMapper.toDirectMessageDto(any(Dm.class))).thenReturn(dmDto);
		when(objectMapper.writeValueAsString(any())).thenReturn("{json}");
		doThrow(new RuntimeException("알림 실패")).when(notificationService).createAndSend(any(NotificationDto.class));

		// when, then
		assertThatThrownBy(() -> dmService.sendDirectMessage(dmRequest))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOTIFICATION_CREATE_FAILED);
	}

	//getDms
	@Test
	@DisplayName("getDms - 커서 O, idAfter X")
	void getDms_cursorOnly(){
		//given
		String cursor = UUID.randomUUID().toString();
		UUID idAfter = UUID.randomUUID();
		List<Dm> dms = makeDmList(2);
		when(dmRepository.findAllByUserIdAndOtherIdAfterCursor(eq(sender.getId()), eq(receiver.getId()), eq(UUID.fromString(cursor)), any(Pageable.class)))
			.thenReturn(dms);
		when(directMessageMapper.toDirectMessageDto(any(Dm.class))).thenReturn(dmDto);

		//when
		dmService.getDms(sender.getId(), receiver.getId(), cursor, idAfter, 5);

		//then
		verify(dmRepository,times(1))
			.findAllByUserIdAndOtherIdAfterCursor(eq(sender.getId()), eq(receiver.getId()), eq(UUID.fromString(cursor)), any(Pageable.class));
	}

	@Test
	@DisplayName("getDms - 커서 X, idAfter O")
	void getDms_idAfterOnly() {
		// given
		String cursor = null;
		UUID idAfter = UUID.randomUUID();
		List<Dm> dms = makeDmList(1);
		when(dmRepository.findAllByUserIdAndOtherIdAfterCursor(eq(sender.getId()), eq(receiver.getId()),
			eq(idAfter), any(Pageable.class))).thenReturn(dms);
		when(directMessageMapper.toDirectMessageDto(any(Dm.class)))
			.thenReturn(dmDto);

		// when
		dmService.getDms(sender.getId(), receiver.getId(), cursor, idAfter, 5);

		// then
		verify(dmRepository, times(1))
			.findAllByUserIdAndOtherIdAfterCursor(eq(sender.getId()), eq(receiver.getId()), eq(idAfter), any(Pageable.class));
	}

	@Test
	@DisplayName("getDms - 커서 O, idAfter O")
	void getDms_bothCursorAndIdAfter() {
		// given
		String cursor = UUID.randomUUID().toString();
		UUID idAfter = UUID.randomUUID();
		List<Dm> dms = makeDmList(2);
		when(dmRepository.findAllByUserIdAndOtherIdAfterCursor(eq(sender.getId()), eq(receiver.getId()),
			eq(UUID.fromString(cursor)), any(Pageable.class))).thenReturn(dms);
		when(directMessageMapper.toDirectMessageDto(any(Dm.class)))
			.thenReturn(dmDto);

		// when
		dmService.getDms(sender.getId(), receiver.getId(), cursor, idAfter, 10);

		// then
		verify(dmRepository, times(1))
			.findAllByUserIdAndOtherIdAfterCursor(eq(sender.getId()), eq(receiver.getId()), eq(UUID.fromString(cursor)), any(Pageable.class));
	}

	@Test
	@DisplayName("getDms - 커서 X, idAfter X")
	void getDms_bothNull() {
		// given
		String cursor = null;
		UUID idAfter = null;
		List<Dm> dms = makeDmList(3);
		when(dmRepository.findAllByUserIdAndOtherIdAfterCursor(eq(sender.getId()), eq(receiver.getId()),
			eq(null), any(Pageable.class))).thenReturn(dms);
		when(directMessageMapper.toDirectMessageDto(any(Dm.class)))
			.thenReturn(dmDto);

		// when
		dmService.getDms(sender.getId(), receiver.getId(), cursor, idAfter, 3);

		// then
		verify(dmRepository, times(1))
			.findAllByUserIdAndOtherIdAfterCursor(eq(sender.getId()), eq(receiver.getId()), eq(null), any(Pageable.class));
	}
}
