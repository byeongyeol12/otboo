package com.codeit.otboo.domain.follow.mapper;

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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.codeit.otboo.domain.follow.dto.FollowDto;
import com.codeit.otboo.domain.follow.entity.Follow;
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
public class FollowMapperTest {

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
	private final FollowMapper followMapper = Mappers.getMapper(FollowMapper.class);

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
	@DisplayName("toFollowDto - 엔티티 -> dto 정상 변환")
	void toFollowDto_success() {
		//given
		Follow follow = Follow.builder()
			.follower(follower)
			.followee(followee)
			.build();

		//when
		FollowDto followDto = followMapper.toFollowDto(follow);

		//then
		assertThat(followDto.follower().id()).isEqualTo(follower.getId());
		assertThat(followDto.followee().id()).isEqualTo(followee.getId());
	}

	@Test
	@DisplayName("toFollowDtoList - 엔티티 -> dto 정상 변환")
	void toFollowDtoList_success() {
		//given
		Follow follow = Follow.builder()
			.follower(follower)
			.followee(followee)
			.build();

		//when
		List<FollowDto> followDtoList = followMapper.toFollowDtoList(List.of(follow));

		//then
		assertThat(followDtoList.get(0).follower().id()).isEqualTo(follower.getId());
		assertThat(followDtoList.get(0).followee().id()).isEqualTo(followee.getId());
	}

	@Test
	@DisplayName("toUserSummary - 유저 프로필이 null 일 때도 정상 동작")
	void toUserSummary_nullProfile_success() {
		//given

		//when
		UserSummaryDto summaryDto = followMapper.toUserSummary(follower);

		//then
		assertThat(summaryDto.name()).isEqualTo(follower.getName());
		assertThat(summaryDto.profileImageUrl()).isNull();
	}


}
