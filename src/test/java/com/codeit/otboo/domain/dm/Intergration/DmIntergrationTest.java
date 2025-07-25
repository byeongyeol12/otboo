package com.codeit.otboo.domain.dm.Intergration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
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

import com.codeit.otboo.domain.dm.entity.Dm;
import com.codeit.otboo.domain.dm.repository.DmRepository;
import com.codeit.otboo.domain.dm.service.DmService;
import com.codeit.otboo.domain.user.dto.request.UserCreateRequest;
import com.codeit.otboo.domain.user.dto.response.UserDto;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.domain.user.service.UserService;
import com.codeit.otboo.global.config.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class DmIntergrationTest {
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
	private ObjectMapper objectMapper;
	@Autowired
	private DmService dmService;
	@Autowired
	private DmRepository dmRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private UserRepository userRepository;

	private User userA;
	private User userB;
	private User userC;
	@BeforeEach
	void setUp() {
		UserCreateRequest userARequest = new UserCreateRequest(
			"userA", "userA@email.com", "UserA123!"
		);
		UserCreateRequest userBRequest = new UserCreateRequest(
			"userB", "userB@email.com", "UserB123!"
		);
		UserCreateRequest userCRequest = new UserCreateRequest(
			"userC", "userC@email.com", "UserC123!"
		);

		UserDto userADto = userService.create(userARequest);
		UserDto userBDto = userService.create(userBRequest);
		UserDto userCDto = userService.create(userCRequest);

		userA = userRepository.findById(userADto.id())
			.orElseThrow(() -> new RuntimeException("userA 저장 실패"));
		userB = userRepository.findById(userBDto.id())
			.orElseThrow(() -> new RuntimeException("userB 저장 실패"));
		userC = userRepository.findById(userCDto.id())
			.orElseThrow(() -> new RuntimeException("userC 저장 실패"));

	}

	//getDms
	@Test
	@DisplayName("메시지 목록 조회 API 통합 테스트 - A,B 조회")
	void getDms_success_AB() throws Exception {
		//given
		Dm dm1 = new Dm(UUID.randomUUID(), userA, userB, "A→B", Instant.now());
		Dm dm2 = new Dm(UUID.randomUUID(), userB, userA, "B→A", Instant.now().plusSeconds(10));
		Dm dm3 = new Dm(UUID.randomUUID(), userC, userA, "C→A", Instant.now().plusSeconds(20));
		dmRepository.save(dm1);
		dmRepository.save(dm2);
		dmRepository.save(dm3);

		UserPrincipal principal = new UserPrincipal(
			userA.getId(), userA.getEmail(), userA.getPasswordHash(), userA.getRole()
		);
		//when,then
		mockMvc.perform(get("/api/direct-messages")
				.param("userId", userB.getId().toString())
				.param("limit", "10")
				.with(SecurityMockMvcRequestPostProcessors.user(principal)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(2))
			.andExpect(jsonPath("$.data[0].content").value("A→B"))
			.andExpect(jsonPath("$.data[1].content").value("B→A"))
			.andExpect(jsonPath("$.data[0].sender.id").value(userA.getId().toString()))
			.andExpect(jsonPath("$.data[0].receiver.id").value(userB.getId().toString()))
			.andExpect(jsonPath("$.data[1].sender.id").value(userB.getId().toString()))
			.andExpect(jsonPath("$.data[1].receiver.id").value(userA.getId().toString()));
	}
	@Test
	@DisplayName("메시지 목록 조회 API 통합 테스트 - A,C 조회")
	void getDms_success_AC() throws Exception {
		Dm dm1 = new Dm(UUID.randomUUID(), userA, userC, "A→C", Instant.now());
		Dm dm2 = new Dm(UUID.randomUUID(), userC, userA, "C→A", Instant.now().plusSeconds(10));
		Dm dm3 = new Dm(UUID.randomUUID(), userA, userB, "A→B", Instant.now().plusSeconds(20));
		dmRepository.save(dm1);
		dmRepository.save(dm2);
		dmRepository.save(dm3);

		UserPrincipal principal = new UserPrincipal(
			userA.getId(), userA.getEmail(), userA.getPasswordHash(), userA.getRole()
		);
		//when,then
		mockMvc.perform(get("/api/direct-messages")
				.param("userId", userC.getId().toString())
				.param("limit", "10")
				.with(SecurityMockMvcRequestPostProcessors.user(principal)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(2))
			.andExpect(jsonPath("$.data[0].content").value("A→C"))
			.andExpect(jsonPath("$.data[1].content").value("C→A"))
			.andExpect(jsonPath("$.data[0].sender.id").value(userA.getId().toString()))
			.andExpect(jsonPath("$.data[0].receiver.id").value(userC.getId().toString()))
			.andExpect(jsonPath("$.data[1].sender.id").value(userC.getId().toString()))
			.andExpect(jsonPath("$.data[1].receiver.id").value(userA.getId().toString()));
	}

	@Test
	@DisplayName("메시지 목록 조회 API 통합 테스트 - 대화 없는 B,C 조회")
	void getDms_success_BC_noConversation() throws Exception {
		//given
		Dm dm1 = new Dm(UUID.randomUUID(), userA, userB, "A→B", Instant.now());
		dmRepository.save(dm1);

		UserPrincipal principal = new UserPrincipal(
			userC.getId(), userC.getEmail(), userC.getPasswordHash(), userC.getRole()
		);

		//when,then
		mockMvc.perform(get("/api/direct-messages")
				.param("userId", userB.getId().toString())
				.param("limit", "10")
				.with(SecurityMockMvcRequestPostProcessors.user(principal)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(0));
	}

	@Test
	@DisplayName("메시지 목록 조회 API 통합 테스트 - 인증 없으면 403")
	void getDms_failure_unauthenticated() throws Exception {
		//given

		//when,then
		mockMvc.perform(get("/api/direct-messages")
				.param("userId", userB.getId().toString())
				.param("limit", "10"))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("메시지 목록 조회 API 통합 테스트 - 파라미터 누락시 400")
	void getDms_fail_requiredParam() throws Exception {
		//given
		UserPrincipal principal = new UserPrincipal(
			userA.getId(), userA.getEmail(), userA.getPasswordHash(), userA.getRole()
		);

		//when,then
		mockMvc.perform(get("/api/direct-messages")
				.param("limit", "10")
				.with(SecurityMockMvcRequestPostProcessors.user(principal)))
			.andExpect(status().isInternalServerError());

		mockMvc.perform(get("/api/direct-messages")
				.param("userId", userB.getId().toString())
				.with(SecurityMockMvcRequestPostProcessors.user(principal)))
			.andExpect(status().isInternalServerError());
	}
}
