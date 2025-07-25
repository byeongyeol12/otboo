package com.codeit.otboo.domain.notification.intergration;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.notification.entity.NotificationLevel;
import com.codeit.otboo.domain.notification.repository.NotificationRepository;
import com.codeit.otboo.domain.user.dto.request.UserCreateRequest;
import com.codeit.otboo.domain.user.dto.response.UserDto;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.domain.user.service.UserService;
import com.codeit.otboo.global.config.security.UserPrincipal;
import com.codeit.otboo.global.enumType.Role;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class NotificationIntergrationTest {
	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
		.withDatabaseName("test-db")
		.withUsername("test-user")
		.withPassword("test-pass");
	@Container
	static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(
		6379);

	@DynamicPropertySource
	static void overrideProps(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
		registry.add("jwt.secret", () -> "this-is-a-super-secret-key-for-test-purpose-only-12345");
		registry.add("jwt.expiration", () -> "600000");
		registry.add("jwt.refresh-token-validity-in-ms", () -> "604800000");
		registry.add("api.kma.service-key", () -> "TEST_KMA_KEY");
		registry.add("api.kma.base-url", () -> "https://test.kma.api");
		registry.add("API_KAKAO_REST_KEY", () -> "TEST_KAKAO_KEY");
		registry.add("spring.data.redis.host", redis::getHost);
		registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	private User user;

	@BeforeEach
	void setUp() {
		UserCreateRequest userRequest = new UserCreateRequest(
			"user", "user@email.com", "User123!"
		);
		UserDto userDto = userService.create(userRequest);

		user = userRepository.findById(userDto.id()).orElseThrow(() -> new RuntimeException("유저 저장 실패"));
	}

	//getNotifications
	@Test
	@DisplayName("알림 목록 조회 API 통합 테스트")
	void getNotifications_success() throws Exception {
		//given
		Notification notification = Notification.builder()
			.title("Test")
			.content("Test-content")
			.level(NotificationLevel.INFO)
			.confirmed(false)
			.receiver(user)
			.build();
		notificationRepository.save(notification);

		UserPrincipal userPrincipal = new UserPrincipal(
			user.getId(), user.getEmail(), user.getPasswordHash(), Role.USER
		);

		//when,then
		mockMvc.perform(get("/api/notifications")
				.with(user(userPrincipal))
				.param("limit", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].title").value("Test"));

	}

	@Test
	@DisplayName("알림 목록 조회 API 통합 테스트 실패 - 필수 파라미터 누락")
	void getNotifications_failure_requiredParam() throws Exception {
		//given
		UserPrincipal userPrincipal = new UserPrincipal(
			user.getId(), user.getEmail(), user.getPasswordHash(), Role.USER
		);

		//when,then
		mockMvc.perform(get("/api/notifications")
				.with(user(userPrincipal)))
			.andExpect(status().isInternalServerError());
	}

	//readNotifications
	@Test
	@DisplayName("알림 읽음 처리 API 통합 테스트")
	void readNotifications_success() throws Exception {
		//given
		Notification notification = Notification.builder()
			.title("Test")
			.content("Test-content")
			.level(NotificationLevel.INFO)
			.confirmed(false)
			.receiver(user)
			.build();
		Notification saved = notificationRepository.save(notification);

		UserPrincipal userPrincipal = new UserPrincipal(
			user.getId(), user.getEmail(), user.getPasswordHash(), Role.USER
		);

		//when,then
		mockMvc.perform(delete("/api/notifications/{notificationId}", saved.getId())
				.with(user(userPrincipal)))
			.andExpect(status().isNoContent());
		assertThat(notificationRepository.findById(saved.getId()).orElseThrow().isConfirmed()).isTrue();
	}

	@Test
	@DisplayName("알림 읽음 처리 API 통합 테스트 실패 - 존재하지 않는 알림")
	void readNotifications_failure_notFound() throws Exception {
		//given
		UserPrincipal userPrincipal = new UserPrincipal(
			user.getId(), user.getEmail(), user.getPasswordHash(), Role.USER
		);
		UUID notExistId = UUID.randomUUID();

		//when,then
		mockMvc.perform(delete("/api/notifications/{notificationId}", notExistId)
				.with(user(userPrincipal)))
			.andExpect(status().isNotFound());
	}
}
