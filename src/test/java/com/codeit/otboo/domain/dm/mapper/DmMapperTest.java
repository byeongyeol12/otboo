package com.codeit.otboo.domain.dm.mapper;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;

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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.codeit.otboo.domain.dm.dto.DirectMessageDto;
import com.codeit.otboo.domain.dm.entity.Dm;
import com.codeit.otboo.domain.dm.repository.DmRepository;
import com.codeit.otboo.domain.user.dto.response.UserSummaryDto;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.global.config.QueryDslConfig;
import com.codeit.otboo.global.enumType.Role;

@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DataJpaTest
@Import(QueryDslConfig.class)
@EnableJpaAuditing
public class DmMapperTest {

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
	}

	private final DirectMessageMapper directMessageMapper = Mappers.getMapper(DirectMessageMapper.class);

	@Autowired
	DmRepository dmRepository;

	@Autowired
	UserRepository userRepository;

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
	}

	@Test
	@DisplayName("toDirectMessageDto - 엔티티 -> dto 정상 변환")
	void toDirectMessageDto_success() {
		//given
		Dm dm = new Dm(
			UUID.randomUUID(),sender,receiver,"content", Instant.now()
		);
		//when
		DirectMessageDto directMessageDto = directMessageMapper.toDirectMessageDto(dm);

		//then
		assertThat(directMessageDto).isNotNull();
		assertThat(directMessageDto.content()).isEqualTo("content");
	}
	@Test
	@DisplayName("toUserSummary - User -> UserSummaryDto 정상 변환")
	void toUserSummary_success() {
		//given
		User user = new User();
		user.setName("user");
		user.setEmail("user@email.com");
		user.setPasswordHash("pw1");
		user.setRole(Role.USER);

		//when
		UserSummaryDto userSummaryDto = directMessageMapper.toUserSummary(user);

		//then
		assertThat(userSummaryDto.id()).isEqualTo(user.getId());
		assertThat(userSummaryDto.name()).isEqualTo(user.getName());
	}
}
