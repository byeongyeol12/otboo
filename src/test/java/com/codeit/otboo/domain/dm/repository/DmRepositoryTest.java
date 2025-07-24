package com.codeit.otboo.domain.dm.repository;

import static org.assertj.core.api.Assertions.*;

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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.codeit.otboo.domain.dm.entity.Dm;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.global.config.QueryDslConfig;
import com.codeit.otboo.global.enumType.Role;

@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@Import(QueryDslConfig.class)
@EnableJpaAuditing
public class DmRepositoryTest {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
		.withDatabaseName("test-db")
		.withUsername("test-user")
		.withPassword("test-pass");

	@DynamicPropertySource
	static void overrideProps(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);

		// (필요시) Redis 정보도 동적으로 주입
		// registry.add("spring.data.redis.host", redis::getHost);
		// registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
	}

	@Autowired
	private DmRepository dmRepository;

	@Autowired
	private UserRepository userRepository;

	private User sender;
	private User receiver;

	@BeforeEach
	void setUp() {
		sender = new User();
		sender.setName("sender");
		sender.setEmail("sender@email.com");
		sender.setPasswordHash("pw1");
		sender.setRole(Role.USER);
		sender.setField("T1");
		userRepository.save(sender);

		receiver = new User();
		receiver.setName("receiver");
		receiver.setEmail("receiver@email.com");
		receiver.setPasswordHash("pw2");
		receiver.setRole(Role.USER);
		receiver.setField("T2");
		userRepository.save(receiver);

		UUID id1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
		UUID id2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
		UUID id3 = UUID.fromString("00000000-0000-0000-0000-000000000003");

		dmRepository.save(Dm.builder()
			.id(id1)
			.sender(sender)
			.receiver(receiver)
			.content("test content 1")
			.build());
		dmRepository.save(Dm.builder()
			.id(id2)
			.sender(receiver)
			.receiver(sender)
			.content("test content 2")
			.build());
		dmRepository.save(Dm.builder()
			.id(id3)
			.sender(sender)
			.receiver(receiver)
			.content("test content 3")
			.build());
	}

	//findAllByUserIdAndOtherIdAfterCursor
	@Test
	@DisplayName("findAllByUserIdAndOtherIdAfterCursor - 모든 메시지 createdAt,오름차순 조회")
	void findAllByUserIdAndOtherIdAfterCursor_usedCreatedAtASC() {
		//given
		Pageable pageable = PageRequest.of(0, 10);

		//when
		List<Dm> result = dmRepository.findAllByUserIdAndOtherIdAfterCursor(sender.getId(), receiver.getId(), null,
			pageable);

		//then
		assertThat(result).hasSize(3);
		assertThat(result.get(0).getCreatedAt()).isBefore(result.get(1).getCreatedAt());
		assertThat(result.get(1).getCreatedAt()).isBefore(result.get(2).getCreatedAt());
	}

	@Test
	@DisplayName("findAllByUserIdAndOtherIdAfterCursor - idAfter 이후 데이터만 조회")
	void findAllByUserIdAndOtherIdAfterCursor_usedIdAfter() {
		//given
		Pageable pageable = PageRequest.of(0, 10);
		List<Dm> all = dmRepository.findAllByUserIdAndOtherIdAfterCursor(sender.getId(), receiver.getId(), null,
			pageable);
		UUID cursorId = all.get(0).getId();

		//when
		List<Dm> result = dmRepository.findAllByUserIdAndOtherIdAfterCursor(sender.getId(), receiver.getId(), cursorId,
			pageable);

		//then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getId()).isNotEqualTo(cursorId);
	}

	@Test
	@DisplayName("findAllByUserIdAndOtherIdAfterCursor - DM 이 없으면 빈 리스트 조회")
	void findAllByUserIdAndOtherIdAfterCursor_noDm_returnEmptyList() {
		//given
		Pageable pageable = PageRequest.of(0, 10);
		User anotherUser = new User();
		anotherUser.setName("anotherUser");
		anotherUser.setEmail("anotherUser@email.com");
		anotherUser.setPasswordHash("a1");
		anotherUser.setRole(Role.USER);
		anotherUser.setField("T1");

		//when
		List<Dm> result = dmRepository.findAllByUserIdAndOtherIdAfterCursor(sender.getId(), anotherUser.getId(), null,
			pageable);

		//then
		assertThat(result).isEmpty();
	}
}
