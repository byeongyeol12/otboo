package com.codeit.otboo.domain.notification.mapper;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
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
public class NotificationMapperTest {
	private final NotificationMapper notificationMapper = Mappers.getMapper(NotificationMapper.class);

	@Autowired
	UserRepository userRepository;

	private User follower,followee;

	@BeforeEach
	void setUp() {
		follower = new User();
		follower.setEmail("follower@example.com");
		follower.setName("유저1");
		follower.setPasswordHash("pw1");
		follower.setRole(Role.USER);
		follower.setField("IT");
		userRepository.save(follower);

		followee = new User();
		followee.setEmail("followee@example.com");
		followee.setName("유저2");
		followee.setPasswordHash("pw2");
		followee.setRole(Role.USER);
		followee.setField("MKT");
		userRepository.save(followee);
	}

	@Test
	@DisplayName("toNotificationDto - 엔티티 -> DTO 정상 변환")
	void toNotificationDto_success() {
		//given
		Notification notification = Notification.builder()
			.receiver(follower)
			.title("title")
			.content("content")
			.level(NotificationLevel.INFO)
			.confirmed(false)
			.build();

		//when
		NotificationDto notificationDto = notificationMapper.toNotificationDto(notification);

		//then
		assertThat(notificationDto.receiverId()).isEqualTo(follower.getId());
		assertThat(notificationDto.title()).isEqualTo("title");
		assertThat(notificationDto.content()).isEqualTo("content");
		assertThat(notificationDto.level()).isEqualTo(NotificationLevel.INFO);
	}
	@Test
	@DisplayName("toNotificationDtoList - 정상 변환")
	void toNotificationDtoList_success() {
		//given
		Notification notification = Notification.builder()
			.receiver(follower)
			.title("title")
			.content("content")
			.level(NotificationLevel.INFO)
			.confirmed(false)
			.build();

		//when
		List<NotificationDto> notificationDtoList = notificationMapper.toNotificationDtoList(List.of(notification));

		//then
		assertThat(notificationDtoList.get(0).receiverId()).isEqualTo(follower.getId());
		assertThat(notificationDtoList.get(0).title()).isEqualTo("title");
		assertThat(notificationDtoList.get(0).content()).isEqualTo("content");
		assertThat(notificationDtoList.get(0).level()).isEqualTo(NotificationLevel.INFO);
	}
}
