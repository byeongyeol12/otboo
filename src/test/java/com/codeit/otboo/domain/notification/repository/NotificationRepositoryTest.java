package com.codeit.otboo.domain.notification.repository;

import static com.codeit.otboo.domain.notification.entity.NotificationLevel.*;
import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

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

		// 알림 데이터
		for (int i = 0; i < 5; i++) {
			notificationRepository.save(Notification.builder()
				.receiver(user1)
				.title("알림 " + i)
				.content("내용 " + i)
				.level(INFO)
				.confirmed(i % 2 == 0)
				.build());
		}
		notificationRepository.save(Notification.builder()
			.receiver(user2)
			.title("다른 사용자 알림")
			.content("다른 내용")
			.level(WARNING)
			.confirmed(false)
			.build());
	}

	@Test
	@DisplayName("findByReceiverIdAndIdGreaterThanOrderByCreatedAt - 페이징 정렬 성공")
	void findByReceiverIdGreaterThanOrderByCreatedAt_success() {
		//given
		// given: user1의 알림 중 첫 번째 알림을 커서로 삼는다
		List<Notification> allNoti = notificationRepository.findAll();
		Notification cursor = allNoti.stream()
			.filter(n -> n.getReceiver().getId().equals(user1.getId()))
			.sorted(Comparator.comparing(Notification::getCreatedAt)) // 생성순으로 정렬
			.findFirst()
			.orElseThrow();
		Pageable pageable = PageRequest.of(0, 10);

		// when: 커서 알림 id 이후(user1만!) 알림 조회
		List<Notification> result = notificationRepository.findByReceiverIdAndIdGreaterThanOrderByCreatedAt(
			user1.getId(), cursor.getId(), pageable);

		// then: 모두 user1의 알림이고, 커서 id 이후만 조회됨
		assertThat(result).isNotEmpty(); // 비어있지 않아야 함
		assertThat(result)
			.allMatch(n -> n.getReceiver().getId().equals(user1.getId()) && n.getId().compareTo(cursor.getId()) > 0);
		// 생성순 정렬 확인
		List<Instant> createdAtList = result.stream().map(Notification::getCreatedAt).toList();
		assertThat(createdAtList).isSorted();
	}

	@Test
	@DisplayName("findByReceiverIdAndConfirmedFalse - 확인하지 않은 알림 조회")
	void findByReceiverIdAndConfirmedFalse_success() {
		//given
		Pageable pageable = PageRequest.of(0, 10);

		//when
		List<Notification> result = notificationRepository
			.findByReceiverIdAndConfirmedFalse(user1.getId(), pageable);

		//then
		assertThat(result)
			.allMatch(n -> !n.isConfirmed() && n.getReceiver().getId().equals(user1.getId()));
	}

	@Test
	@DisplayName("countByReceiverId - 사용자의 전체 알림 개수 반환")
	void countByReceiverId_success() {
		//given
		long count1 = notificationRepository.countByReceiverId(user1.getId());
		long count2 = notificationRepository.countByReceiverId(user2.getId());

		//when,then
		assertThat(count1).isEqualTo(5);
		assertThat(count2).isEqualTo(1);
	}
}
