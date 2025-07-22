package com.codeit.otboo.domain.notification.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.notification.entity.NotificationLevel;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.global.config.QueryDslConfig;
import com.codeit.otboo.global.enumType.Role;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DataJpaTest
@Import(QueryDslConfig.class)
@EnableJpaAuditing
public class NotificationRepositoryTest {
	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private UserRepository userRepository;

	private User user1;
	private User user2;

	@BeforeEach
	void setUp() {
		// 유저
		user1 = new User();
		user1.setName("user1");
		user1.setEmail("user1@email.com");
		user1.setPasswordHash("pw1");
		user1.setRole(Role.USER);
		user1.setField("T1");
		userRepository.save(user1);

		user2 = new User();
		user2.setName("user2");
		user2.setEmail("user2@email.com");
		user2.setPasswordHash("pw2");
		user2.setRole(Role.USER);
		user2.setField("T2");
		userRepository.save(user2);

		// 알림 데이터 생성: user1(읽음 2, 안읽음 3), user2(안읽음 1)
		for (int i = 0; i < 5; i++) {
			boolean confirmed = (i < 2); // 0,1번은 읽음(true), 2,3,4는 안읽음(false)
			notificationRepository.save(Notification.builder()
				.receiver(user1)
				.title("user1 알림 " + i)
				.content("user1 내용 " + i)
				.level(NotificationLevel.INFO)
				.confirmed(confirmed)
				.build());
		}
		notificationRepository.save(Notification.builder()
			.receiver(user2)
			.title("user2 알림")
			.content("user2 내용")
			.level(NotificationLevel.WARNING)
			.confirmed(false)
			.build());
	}

	@Test
	@DisplayName("findByReceiverIdAndConfirmedFalseOrderByCreatedAtDesc - 페이징 정렬 성공")
	void findByReceiverIdAndConfirmedFalseOrderByCreatedAtDesc_success() {
		// given
		Pageable pageable = PageRequest.of(0, 10);

		// when
		List<Notification> result = notificationRepository
			.findByReceiverIdAndConfirmedFalseOrderByCreatedAtDesc(user1.getId(), pageable);

		// then
		assertThat(result).hasSize(3); // user1 읽지 않은 알림 3개
		assertThat(result).allMatch(n -> n.getReceiver().getId().equals(user1.getId()) && !n.isConfirmed());
		// 최신순 검증
		List<Instant> createdAtList = result.stream().map(Notification::getCreatedAt).toList();
		assertThat(createdAtList).isSortedAccordingTo(Comparator.reverseOrder());
	}

	@Test
	@DisplayName("findByReceiverIdAndConfirmedFalseAndCreatedAtLessThanOrderByCreatedAtDesc - 읽지 않은 알림, 커서(createdAt) 기준 다음 페이지 조회")
	void findByReceiverIdAndConfirmedFalseAndCreatedAtLessThanOrderByCreatedAtDesc_success() {
		// given
		Pageable pageable = PageRequest.of(0, 10);
		List<Notification> firstPage = notificationRepository
			.findByReceiverIdAndConfirmedFalseOrderByCreatedAtDesc(user1.getId(), PageRequest.of(0, 1));
		Instant cursor = firstPage.get(0).getCreatedAt();

		// when: 커서보다 과거(더 오래된) 알림 조회
		List<Notification> result = notificationRepository
			.findByReceiverIdAndConfirmedFalseAndCreatedAtLessThanOrderByCreatedAtDesc(
				user1.getId(), cursor, pageable);

		// then
		assertThat(result).hasSize(2); // 남은 안읽음 2개
		assertThat(result).allMatch(n -> n.getReceiver().getId().equals(user1.getId()) && !n.isConfirmed());
		assertThat(result).allMatch(n -> n.getCreatedAt().isBefore(cursor));
	}

	@Test
	@DisplayName("findByReceiverIdAndConfirmedFalseOrderByCreatedAtDesc - 읽지 않은 알림이 없으면 빈 리스트")
	void findByReceiverIdAndConfirmedFalseOrderByCreatedAtDesc_empty() {
		// given: 존재하지 않는 유저
		Pageable pageable = PageRequest.of(0, 10);

		// when
		List<Notification> result = notificationRepository
			.findByReceiverIdAndConfirmedFalseOrderByCreatedAtDesc(UUID.randomUUID(), pageable);

		// then
		assertThat(result).isEmpty();
	}


	@Test
	@DisplayName("findByReceiverIdAndConfirmedFalseAndCreatedAtLessThanOrderByCreatedAtDesc - 읽지 않은 알림 커서 조회 : 데이터가 없으면 빈 리스트")
	void findByReceiverIdAndConfirmedFalseAndCreatedAtLessThanOrderByCreatedAtDesc_empty() {
		// given: 과거 시간 커서
		Pageable pageable = PageRequest.of(0, 10);
		Instant oldCursor = Instant.now().minusSeconds(3600 * 24);

		// when
		List<Notification> result = notificationRepository
			.findByReceiverIdAndConfirmedFalseAndCreatedAtLessThanOrderByCreatedAtDesc(
				user1.getId(), oldCursor, pageable);

		// then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("countByReceiverIdAndConfirmedFalse - 읽지 않은 알림 개수 카운트")
	void countByReceiverIdAndConfirmedFalse_success() {
		// when
		long countUser1 = notificationRepository.countByReceiverIdAndConfirmedFalse(user1.getId());
		long countUser2 = notificationRepository.countByReceiverIdAndConfirmedFalse(user2.getId());
		long countNotExist = notificationRepository.countByReceiverIdAndConfirmedFalse(UUID.randomUUID());

		// then
		assertThat(countUser1).isEqualTo(3);
		assertThat(countUser2).isEqualTo(1);
		assertThat(countNotExist).isZero();
	}

}
