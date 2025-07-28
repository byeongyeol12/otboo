package com.codeit.otboo.domain.user.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.codeit.otboo.config.TestAuditingConfig;
import com.codeit.otboo.domain.user.dto.request.UserSearchRequest;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.global.enumType.Role;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestAuditingConfig.class, UserSearchRepositoryImplTest.TestConfig.class})
@EnableJpaAuditing
@ActiveProfiles("test")
@Testcontainers
class UserSearchRepositoryImplTest {

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

	@Autowired
	private TestEntityManager em;

	@Autowired
	private UserSearchRepository userSearchRepository;

	@Test
	@DisplayName("기본 조건으로 사용자 검색")
	void 기본조건_검색() {
		User user = new User();
		user.setEmail("alice@ootd.com");
		user.setName("앨리스");
		user.setPasswordHash("123");
		user.setRole(Role.USER);
		user.setLocked(false);
		em.persist(user);

		UserSearchRequest request = new UserSearchRequest();
		request.setLimit(10);
		request.setSortBy("email");
		request.setSortDirection("ASC");

		List<User> result = userSearchRepository.search(request);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getEmail()).isEqualTo("alice@ootd.com");
	}

	@Test
	@DisplayName("이메일 키워드 필터 테스트")
	void 이메일_검색_필터() {
		User user1 = new User();
		user1.setEmail("search@ootd.com");
		user1.setName("검색");
		user1.setPasswordHash("pw");
		user1.setRole(Role.USER);
		user1.setLocked(false);
		em.persist(user1);

		User user2 = new User();
		user2.setEmail("other@ootd.com");
		user2.setName("기타");
		user2.setPasswordHash("pw");
		user2.setRole(Role.USER);
		user2.setLocked(false);
		em.persist(user2);

		UserSearchRequest request = new UserSearchRequest();
		request.setEmailLike("search");
		request.setLimit(10);

		List<User> result = userSearchRepository.search(request);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getEmail()).isEqualTo("search@ootd.com");
	}

	@Test
	@DisplayName("역할 필터 테스트")
	void 역할_검색() {
		User admin = new User();
		admin.setEmail("admin@ootd.com");
		admin.setName("관리자");
		admin.setPasswordHash("pw");
		admin.setRole(Role.ADMIN);
		admin.setLocked(false);
		em.persist(admin);

		User user = new User();
		user.setEmail("user@ootd.com");
		user.setName("유저");
		user.setPasswordHash("pw");
		user.setRole(Role.USER);
		user.setLocked(false);
		em.persist(user);

		UserSearchRequest request = new UserSearchRequest();
		request.setRoleEqual(Role.ADMIN);

		List<User> result = userSearchRepository.search(request);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getRole()).isEqualTo(Role.ADMIN);
	}

	@Test
	@DisplayName("계정 잠금 여부 필터 테스트")
	void 잠금여부_필터() {
		User lockedUser = new User();
		lockedUser.setEmail("lock@ootd.com");
		lockedUser.setName("잠김");
		lockedUser.setPasswordHash("pw");
		lockedUser.setRole(Role.USER);
		lockedUser.setLocked(true);
		em.persist(lockedUser);

		User activeUser = new User();
		activeUser.setEmail("active@ootd.com");
		activeUser.setName("활성");
		activeUser.setPasswordHash("pw");
		activeUser.setRole(Role.USER);
		activeUser.setLocked(false);
		em.persist(activeUser);

		UserSearchRequest request = new UserSearchRequest();
		request.setLocked(false);

		List<User> result = userSearchRepository.search(request);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getEmail()).isEqualTo("active@ootd.com");
	}

	@Test
	@DisplayName("사용자 수 카운트 테스트")
	void 사용자_카운트() {
		User user = new User();
		user.setEmail("count@ootd.com");
		user.setName("카운트");
		user.setPasswordHash("pw");
		user.setRole(Role.USER);
		user.setLocked(false);
		em.persist(user);

		UserSearchRequest request = new UserSearchRequest();
		request.setRoleEqual(Role.USER);
		request.setLocked(false);

		long count = userSearchRepository.count(request);

		assertThat(count).isEqualTo(1);
	}

	@TestConfiguration
	static class TestConfig {
		@Bean
		public JPAQueryFactory jpaQueryFactory(EntityManager em) {
			return new JPAQueryFactory(em);
		}

		@Bean
		public UserSearchRepository userSearchRepository(JPAQueryFactory queryFactory) {
			return new UserSearchRepositoryImpl(queryFactory);
		}
	}
}
