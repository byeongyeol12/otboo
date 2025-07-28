// package com.codeit.otboo.domain.dm.integration;
//
// import static org.assertj.core.api.Assertions.*;
//
// import java.lang.reflect.Type;
// import java.util.Arrays;
// import java.util.Comparator;
// import java.util.List;
// import java.util.UUID;
// import java.util.concurrent.CompletableFuture;
// import java.util.concurrent.TimeUnit;
//
// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.web.server.LocalServerPort;
// import org.springframework.messaging.converter.MappingJackson2MessageConverter;
// import org.springframework.messaging.simp.stomp.StompFrameHandler;
// import org.springframework.messaging.simp.stomp.StompHeaders;
// import org.springframework.messaging.simp.stomp.StompSession;
// import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.context.DynamicPropertyRegistry;
// import org.springframework.test.context.DynamicPropertySource;
// import org.springframework.web.socket.WebSocketHttpHeaders;
// import org.springframework.web.socket.client.standard.StandardWebSocketClient;
// import org.springframework.web.socket.messaging.WebSocketStompClient;
// import org.springframework.web.socket.sockjs.client.SockJsClient;
// import org.springframework.web.socket.sockjs.client.Transport;
// import org.springframework.web.socket.sockjs.client.WebSocketTransport;
// import org.testcontainers.containers.GenericContainer;
// import org.testcontainers.containers.PostgreSQLContainer;
// import org.testcontainers.junit.jupiter.Container;
// import org.testcontainers.junit.jupiter.Testcontainers;
// import org.testcontainers.utility.DockerImageName;
//
// import com.codeit.otboo.domain.dm.dto.DirectMessageCreateRequest;
// import com.codeit.otboo.domain.dm.dto.DirectMessageDto;
// import com.codeit.otboo.domain.dm.repository.DmRepository;
// import com.codeit.otboo.domain.notification.repository.NotificationRepository;
// import com.codeit.otboo.domain.user.entity.User;
// import com.codeit.otboo.domain.user.repository.UserRepository;
// import com.codeit.otboo.global.config.jwt.JwtTokenProvider;
// import com.codeit.otboo.global.enumType.Role;
//
// @Testcontainers
// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// @AutoConfigureMockMvc
// @ActiveProfiles("test")
// public class WebsocketIntegrationTest {
// 	@Container
// 	public static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
// 		.withDatabaseName("test-db")
// 		.withUsername("test-user")
// 		.withPassword("test-pass");
//
// 	@Container
// 	public static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
// 		.withExposedPorts(6379);
//
// 	@DynamicPropertySource
// 	static void overrideProps(DynamicPropertyRegistry registry) {
// 		registry.add("spring.datasource.url", () -> postgres.getJdbcUrl());
// 		registry.add("spring.datasource.username", () -> postgres.getUsername());
// 		registry.add("spring.datasource.password", () -> postgres.getPassword());
// 		registry.add("spring.datasource.driver-class-name", () -> postgres.getDriverClassName());
// 		registry.add("jwt.secret", () -> "this-is-a-super-secret-key-for-test-purpose-only-12345");
// 		registry.add("jwt.expiration", () -> "600000");
// 		registry.add("jwt.refresh-token-validity-in-ms", () -> "604800000");
// 		registry.add("api.kma.service-key", () -> "TEST_KMA_KEY");
// 		registry.add("api.kma.base-url", () -> "https://test.kma.api");
// 		registry.add("API_KAKAO_REST_KEY", () -> "TEST_KAKAO_KEY");
// 		registry.add("spring.data.redis.host", () -> redis.getHost());
// 		registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
// 	}
//
// 	@Autowired UserRepository userRepository;
// 	@Autowired DmRepository dmRepository;
// 	@Autowired NotificationRepository notificationRepository;
// 	@Autowired JwtTokenProvider jwtTokenProvider;
// 	@LocalServerPort int port;
//
// 	private String wsUrl;
//
// 	@BeforeEach
// 	void setUpUrl() {
// 		wsUrl = "ws://localhost:" + port + "/ws";
// 	}
//
// 	@AfterEach
// 	void tearDown() {
// 		// 테스트 데이터 정리
// 		dmRepository.deleteAll();
// 		notificationRepository.deleteAll();
// 		userRepository.deleteAll();
// 	}
//
// 	@Test
// 	@DisplayName("실제 DM WebSocket 송수신 통합 테스트 (알림 포함)")
// 	void testDirectMessageSendAndReceiveOverWebSocket() throws Exception {
// 		// 1. 테스트 유저 2명 생성
// 		User sender = new User();
// 		sender.setName("sender");
// 		sender.setEmail("sender@email.com");
// 		sender.setPasswordHash("pw1");
// 		sender.setRole(Role.USER);
// 		sender.setField("T1");
// 		userRepository.save(sender);
//
// 		User receiver = new User();
// 		receiver.setName("receiver");
// 		receiver.setEmail("receiver@email.com");
// 		receiver.setPasswordHash("pw2");
// 		receiver.setRole(Role.USER);
// 		receiver.setField("T2");
// 		userRepository.save(receiver);
// 		userRepository.flush();
//
// 		// 2. JWT 토큰 발급
// 		String senderJwt = jwtTokenProvider.generateToken(sender.getId(), sender.getEmail(), sender.getName(), sender.getRole().name());
// 		String receiverJwt = jwtTokenProvider.generateToken(receiver.getId(), receiver.getEmail(), receiver.getName(), receiver.getRole().name());
//
// 		// 3. WebSocket StompClient 준비 (SockJS 적용)
// 		List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));
// 		SockJsClient sockJsClient = new SockJsClient(transports);
// 		WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
// 		stompClient.setMessageConverter(new MappingJackson2MessageConverter());
//
// 		// 4. DM Key 생성 (오름차순)
// 		List<UUID> sortedIds = Arrays.asList(sender.getId(), receiver.getId());
// 		sortedIds.sort(Comparator.naturalOrder());
// 		String dmKey = sortedIds.get(0) + "_" + sortedIds.get(1);
//
// 		// 5. 수신자 측 STOMP 세션 연결 (토큰 포함)
// 		StompHeaders receiverConnectHeaders = new StompHeaders();
// 		receiverConnectHeaders.add("Authorization", "Bearer " + receiverJwt);
//
// 		StompSession receiverSession = stompClient
// 			.connect(wsUrl, new WebSocketHttpHeaders(), receiverConnectHeaders, new StompSessionHandlerAdapter() {})
// 			.get(2, TimeUnit.SECONDS);
//
// 		// 6. 수신자 측 구독
// 		CompletableFuture<DirectMessageDto> receivedDmFuture = new CompletableFuture<>();
// 		receiverSession.subscribe("/sub/direct-messages_" + dmKey, new StompFrameHandler() {
// 			@Override
// 			public Type getPayloadType(StompHeaders headers) { return DirectMessageDto.class; }
// 			@Override
// 			public void handleFrame(StompHeaders headers, Object payload) {
// 				receivedDmFuture.complete((DirectMessageDto) payload);
// 			}
// 		});
// 		Thread.sleep(300); // subscribe 직후 잠깐 대기
// 		// 7. 발신자 측 STOMP 세션 연결
// 		StompHeaders senderConnectHeaders = new StompHeaders();
// 		senderConnectHeaders.add("Authorization", "Bearer " + senderJwt);
//
// 		StompSession senderSession = stompClient
// 			.connect(wsUrl, new WebSocketHttpHeaders(), senderConnectHeaders, new StompSessionHandlerAdapter() {})
// 			.get(2, TimeUnit.SECONDS);
//
// 		// 8. DM 발송 메시지 생성 및 송신
// 		String content = "안녕! WebSocket DM 테스트";
// 		DirectMessageCreateRequest dmRequest = new DirectMessageCreateRequest(
// 			sender.getId(), receiver.getId(), content
// 		);
// 		senderSession.send("/pub/direct-messages_send", dmRequest);
//
// 		// 9. 수신자 메시지 도착 및 DTO 검증
// 		DirectMessageDto receivedDm = receivedDmFuture.get(3, TimeUnit.SECONDS);
//
// 		assertThat(receivedDm).isNotNull();
// 		assertThat(receivedDm.content()).isEqualTo(content);
// 		assertThat(receivedDm.sender().id()).isEqualTo(sender.getId().toString());
// 		assertThat(receivedDm.receiver().id()).isEqualTo(receiver.getId().toString());
//
// 		// 10. DB 저장 검증
// 		assertThat(dmRepository.findAll()).hasSize(1);
// 		assertThat(dmRepository.findAll().get(0).getContent()).isEqualTo(content);
//
// 		// 11. 알림 생성 여부 검증
// 		assertThat(notificationRepository.findAll().stream()
// 			.anyMatch(n -> n.getReceiver().getId().equals(receiver.getId()) && n.getContent().contains("DM")))
// 			.isTrue();
// 	}
// }
