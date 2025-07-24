package com.codeit.otboo.domain.notification.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.dto.NotificationDtoCursorResponse;
import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.notification.entity.NotificationLevel;
import com.codeit.otboo.domain.notification.mapper.NotificationMapper;
import com.codeit.otboo.domain.notification.repository.NotificationRepository;
import com.codeit.otboo.domain.sse.event.NotificationCreatedEvent;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.enumType.Role;
import com.codeit.otboo.global.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceImplTest {
	@InjectMocks
	private NotificationServiceImpl notificationService;

	@Mock
	private NotificationRepository notificationRepository;
	@Mock
	private NotificationMapper notificationMapper;
	@Mock
	private UserRepository userRepository;
	@Mock
	private ApplicationEventPublisher eventPublisher;

	private User user;
	private Notification notification;
	private NotificationDto notificationDto;

	@BeforeEach
	void setUp() {
		// 유저
		user = new User();
		user.setId(UUID.randomUUID());
		user.setName("user1");
		user.setEmail("user1@email.com");
		user.setPasswordHash("pw1");
		user.setRole(Role.USER);
		user.setField("T1");
		userRepository.save(user);

		//알림
		notification = Notification.builder()
			.receiver(user)
			.title("알림 제목")
			.content("알림 내용")
			.level(NotificationLevel.INFO)
			.confirmed(false)
			.build();

		//알림 DTO
		notificationDto = new NotificationDto(
			UUID.randomUUID(),
			Instant.now(),
			user.getId(),
			notification.getTitle(),
			notification.getContent(),
			notification.getLevel()
		);
	}

	//createAndSend
	@Test
	@DisplayName("createAndSend - 알림 생성 및 전송 성공")
	void createAndSend_success(){
		//given
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
		when(notificationMapper.toNotificationDto(any(Notification.class))).thenReturn(notificationDto);

		//when
		NotificationDto result = notificationService.createAndSend(notificationDto);

		//then
		assertThat(result).isNotNull();
		assertThat(result.receiverId()).isEqualTo(user.getId());
		verify(notificationRepository,times(1)).save(any(Notification.class));
		verify(eventPublisher,times(1)).publishEvent(any(NotificationCreatedEvent.class));
	}

	@Test
	@DisplayName("createAndSend - 유저가 없는 경우 실패")
	void createAndSend_failure(){
		//given
		when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

		//when,then
		CustomException ex = assertThrows(CustomException.class,() ->{
			notificationService.createAndSend(notificationDto);
		});
		assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOTIFICATION_CREATE_FAILED);
	}

	//getNotifications
	@Test
	@DisplayName("getNotifications - 커서/createdAt 없는 경우, 읽지 않은 최신 알림 반환")
	void getNotifications_noCursor_unconfirmed(){
		// given
		List<Notification> notifications = List.of(notification);
		when(notificationRepository.findByReceiverIdAndConfirmedFalseOrderByCreatedAtDesc(eq(user.getId()), any(Pageable.class)))
			.thenReturn(notifications);
		when(notificationRepository.countByReceiverIdAndConfirmedFalse(eq(user.getId()))).thenReturn(1L);
		when(notificationMapper.toNotificationDtoList(anyList())).thenReturn(List.of(notificationDto));

		// when
		NotificationDtoCursorResponse result = notificationService.getNotifications(
			user.getId(), null, null, 1
		);

		// then
		assertThat(result.data()).hasSize(1);
		assertThat(result.totalCount()).isEqualTo(1);
		assertThat(result.nextCursor()).isNull();
		verify(notificationRepository, times(1)).findByReceiverIdAndConfirmedFalseOrderByCreatedAtDesc(eq(user.getId()), any(Pageable.class));
	}

	@Test
	@DisplayName("getNotifications - createdAt 커서(타임스탬프) 있을 때, 그 이전 읽지 않은 알림 반환")
	void getNotifications_withCursor(){
		// given
		String cursor = Instant.now().toString(); // e.g. "2024-07-21T12:00:00Z"
		List<Notification> notifications = List.of(notification);
		when(notificationRepository.findByReceiverIdAndConfirmedFalseAndCreatedAtLessThanOrderByCreatedAtDesc(
			eq(user.getId()), any(Instant.class), any(Pageable.class)))
			.thenReturn(notifications);
		when(notificationRepository.countByReceiverIdAndConfirmedFalse(eq(user.getId()))).thenReturn(1L);
		when(notificationMapper.toNotificationDtoList(anyList())).thenReturn(List.of(notificationDto));

		// when
		NotificationDtoCursorResponse result = notificationService.getNotifications(
			user.getId(), cursor, null, 1
		);

		// then
		assertThat(result.data()).hasSize(1);
		assertThat(result.totalCount()).isEqualTo(1);
		verify(notificationRepository, times(1)).findByReceiverIdAndConfirmedFalseAndCreatedAtLessThanOrderByCreatedAtDesc(
			eq(user.getId()), any(Instant.class), any(Pageable.class));
	}

	@Test
	@DisplayName("getNotifications - 읽지 않은 알림이 없으면 빈 리스트 반환")
	void getNotifications_noUnread() {
		// given
		when(notificationRepository.findByReceiverIdAndConfirmedFalseOrderByCreatedAtDesc(eq(user.getId()), any(Pageable.class)))
			.thenReturn(List.of());
		when(notificationRepository.countByReceiverIdAndConfirmedFalse(eq(user.getId()))).thenReturn(0L);
		when(notificationMapper.toNotificationDtoList(anyList())).thenReturn(List.of());

		// when
		NotificationDtoCursorResponse result = notificationService.getNotifications(
			user.getId(), null, null, 1
		);

		// then
		assertThat(result.data()).isEmpty();
		assertThat(result.totalCount()).isZero();
	}

	//readNotification
	@Test
	@DisplayName("readNotifications - 알림 읽음 성공")
	void readNotifications_success(){
		//given
		Notification unread = Notification.builder()
			.receiver(user)
			.title("읽지않은 알림 제목")
			.content("읽지않은 알림 내용")
			.level(NotificationLevel.INFO)
			.confirmed(false)
			.build();
		when(notificationRepository.findById(unread.getId())).thenReturn(Optional.of(unread));
		when(notificationRepository.save(any(Notification.class))).thenReturn(unread);

		//when
		notificationService.readNotifications(unread.getId(),user.getId());

		//then
		assertTrue(unread.isConfirmed());
		verify(notificationRepository,times(1)).save(any(Notification.class));
	}
	@Test
	@DisplayName("readNotifications - 유저 불일치로 실패")
	void readNotifications_failure(){
		//given
		User user2 = new User();
		user2.setId(UUID.randomUUID());
		user2.setName("user2");
		user2.setEmail("user2@email.com");
		user2.setPasswordHash("pw2");
		user2.setRole(Role.USER);
		user2.setField("T2");
		userRepository.save(user2);

		Notification otherNotification = Notification.builder()
			.receiver(user2)
			.title("알림2")
			.content("내용2")
			.level(NotificationLevel.INFO)
			.confirmed(false)
			.build();

		when(notificationRepository.findById(otherNotification.getId())).thenReturn(Optional.of(otherNotification));

		//when,then
		CustomException ex = assertThrows(CustomException.class, () -> {
			notificationService.readNotifications(otherNotification.getId(), user.getId());
		});
		assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
	}

	@Test
	@DisplayName("readNotifications - 알림이 존재하지 않은 경우")
	void readNotifications_noNotifications(){
		// given
		when(notificationRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

		// when & then
		CustomException ex = assertThrows(CustomException.class, () -> {
			notificationService.readNotifications(UUID.randomUUID(), user.getId());
		});
		assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);
	}
}
