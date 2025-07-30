package com.codeit.otboo.domain.follow.intergration;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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

import com.codeit.otboo.domain.follow.dto.FollowCreateRequest;
import com.codeit.otboo.domain.follow.dto.FollowDto;
import com.codeit.otboo.domain.follow.service.FollowService;
import com.codeit.otboo.domain.user.dto.request.UserCreateRequest;
import com.codeit.otboo.domain.user.dto.response.UserDto;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.domain.user.service.UserService;
import com.codeit.otboo.global.config.security.UserPrincipal;
import com.codeit.otboo.global.enumType.Role;
import com.fasterxml.jackson.databind.ObjectMapper;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class FollowIntegrationTest {

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
	private FollowService followService;
	@Autowired
	private UserService userService;
	@Autowired
	private UserRepository userRepository;

	private User follower;
	private User followee;
	private User anotherUser;

	@BeforeEach
	public void setUp() {
		UserCreateRequest followerRequest = new UserCreateRequest(
			"follower", "follower@email.com", "Follower123!"
		);
		UserCreateRequest followeeRequest = new UserCreateRequest(
			"followee", "followee@email.com", "Followee123!"
		);
		UserCreateRequest anotherRequest = new UserCreateRequest(
			"anotherUser", "anotherUser@email.com", "anotherUser123!"
		);
		UserDto followerDto = userService.create(followerRequest);
		UserDto followeeDto = userService.create(followeeRequest);
		UserDto anotherDto = userService.create(anotherRequest);

		follower = userRepository.findById(followerDto.id()).orElseThrow(() -> new RuntimeException("팔로워 저장 실패"));
		followee = userRepository.findById(followeeDto.id()).orElseThrow(() -> new RuntimeException("팔로이 저장 실패"));
		anotherUser = userRepository.findById(anotherDto.id()).orElseThrow(() -> new RuntimeException("유저 저장 실패"));
	}

	// POST /api/follows - createFollow
	@Test
	@DisplayName("팔로우 생성 API 통합 테스트")
	void createFollow_success() throws Exception {
		//given
		FollowCreateRequest request = new FollowCreateRequest(followee.getId(), follower.getId());
		String requestBody = objectMapper.writeValueAsString(request);
		UserPrincipal userPrincipal = new UserPrincipal(
			follower.getId(),
			follower.getEmail(),
			follower.getPasswordHash(),
			Role.USER
		);
		//when,then
		mockMvc.perform(post("/api/follows")
				.with(user(userPrincipal))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id", notNullValue()))
			.andExpect(jsonPath("$.follower.id", is(follower.getId().toString())))
			.andExpect(jsonPath("$.followee.id", is(followee.getId().toString())));

	}

	@Test
	@DisplayName("팔로우 생성 API 통합 테스트 실패 : 중복 팔로우 생성")
	void createFollow_failure_duplicate() throws Exception {
		//given
		followService.createFollow(follower.getId(), followee.getId());

		FollowCreateRequest request = new FollowCreateRequest(followee.getId(), follower.getId());
		String requestBody = objectMapper.writeValueAsString(request);
		UserPrincipal userPrincipal = new UserPrincipal(
			follower.getId(),
			follower.getEmail(),
			follower.getPasswordHash(),
			Role.USER
		);

		//when,then
		mockMvc.perform(post("/api/follows")
				.with(user(userPrincipal))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isBadRequest());
	}

	//getFollowsSummary
	@Test
	@DisplayName("팔로우 요약 정보 조회 API 통합 테스트")
	void getFollowsSummary_success() throws Exception {
		//given
		UserPrincipal userPrincipal = new UserPrincipal(
			follower.getId(),
			follower.getEmail(),
			follower.getPasswordHash(),
			Role.USER
		);
		followService.createFollow(follower.getId(), followee.getId());

		//when,then
		mockMvc.perform(get("/api/follows/summary")
				.with(user(userPrincipal))
				.param("userId", followee.getId().toString()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.followeeId", is(followee.getId().toString())))
			.andExpect(jsonPath("$.followerCount", is(1)))
			.andExpect(jsonPath("$.followingCount", is(0)))
			.andExpect(jsonPath("$.followedByMe", is(true)))
			.andExpect(jsonPath("$.followingMe", is(false)));
	}

	//getFollowings
	@Test
	@DisplayName("팔로잉 목록 조회 API 통합 테스트")
	void getFollowings_success() throws Exception {
		//given
		UserPrincipal userPrincipal = new UserPrincipal(
			follower.getId(),
			follower.getEmail(),
			follower.getPasswordHash(),
			Role.USER
		);
		followService.createFollow(follower.getId(), followee.getId());
		followService.createFollow(follower.getId(), anotherUser.getId());

		//when,then
		mockMvc.perform(get("/api/follows/followings")
				.with(user(userPrincipal))
				.param("followerId", follower.getId().toString())
				.param("limit", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data", hasSize(2)))
			.andExpect(jsonPath("$.data[*].followee.id", hasItems(
				followee.getId().toString(),
				anotherUser.getId().toString()
			)));
	}

	//getFollowers
	@Test
	@DisplayName("팔로워 목록 조회 API 통합 테스트")
	void getFollowers_success() throws Exception {
		// given
		UserPrincipal userPrincipal = new UserPrincipal(
			followee.getId(),
			followee.getEmail(),
			followee.getPasswordHash(),
			Role.USER
		);

		followService.createFollow(follower.getId(), followee.getId());
		followService.createFollow(anotherUser.getId(), followee.getId());

		// when, then
		mockMvc.perform(get("/api/follows/followers")
				.with(user(userPrincipal))
				.param("followeeId", followee.getId().toString())
				.param("limit", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data", hasSize(2)))
			.andExpect(jsonPath("$.data[*].follower.id", hasItems(
				follower.getId().toString(),
				anotherUser.getId().toString()
			)));
	}

	//cancelFollow
	@Test
	@DisplayName("팔로우 취소 API 통합 테스트")
	void cancelFollow_success() throws Exception {
		//given
		UserPrincipal userPrincipal = new UserPrincipal(
			follower.getId(),
			follower.getEmail(),
			follower.getPasswordHash(),
			Role.USER
		);
		FollowDto follow = followService.createFollow(follower.getId(), followee.getId());

		//when,then
		mockMvc.perform(delete("/api/follows/{followId}", follow.id())
				.with(user(userPrincipal))
				.with(csrf()))
			.andExpect(status().isNoContent());
	}
}
