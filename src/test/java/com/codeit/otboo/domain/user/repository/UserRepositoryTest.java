package com.codeit.otboo.domain.user.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.codeit.otboo.TestApplication;
import com.codeit.otboo.config.TestAuditingConfig;
import com.codeit.otboo.domain.user.entity.Profile;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.global.enumType.Gender;
import com.codeit.otboo.global.enumType.Role;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

@DisplayName("UserRepository 테스트")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestAuditingConfig.class, UserRepositoryTestConfig.class})
@EnableJpaAuditing
@ActiveProfiles("test")
@ContextConfiguration(classes = TestApplication.class)
public class UserRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProfileRepository profileRepository;

	@Autowired
	private TestEntityManager em;

	@Test
	@DisplayName("이메일로 사용자 조회")
	void 이메일로_유저를_조회하기() {
		// given
		User user = new User();
		user.setEmail("test@ootd.com");
		user.setName("테스트");
		user.setPasswordHash("hashed");
		user.setRole(Role.USER);
		user.setLocked(false);

		userRepository.save(user);

		Profile profile = new Profile(user, "테스트닉네임", Gender.MALE);
		profileRepository.save(profile);

		// when
		Optional<User> found = userRepository.findByEmail("test@ootd.com");

		// then
		assertThat(found).isPresent();
		assertThat(found.get().getName()).isEqualTo("테스트");
	}

	@Test
	void 이메일_중복_확인하기() {
		// given
		User user = new User();
		user.setEmail("dup@ootd.com");
		user.setName("중복");
		user.setPasswordHash("hashed");
		user.setRole(Role.USER);
		user.setLocked(false);
		userRepository.save(user);

		Profile profile = new Profile(user, "테스트닉네임2", Gender.MALE);
		profileRepository.save(profile);

		// when
		boolean exists = userRepository.existsByEmail("dup@ootd.com");

		// then
		assertThat(exists).isTrue();
	}

	@Test
	void 역할로_사용자_조회하기() {
		// given
		User admin = new User();
		admin.setEmail("admin@ootd.com");
		admin.setName("관리자");
		admin.setPasswordHash("hashed");
		admin.setRole(Role.ADMIN);
		admin.setLocked(false);
		userRepository.save(admin);

		Profile profile = new Profile(admin, "테스트닉네임3", Gender.MALE);
		profileRepository.save(profile);

		// when
		List<User> admins = userRepository.findByRole(Role.ADMIN);

		// then
		boolean containsAdminEmail = admins.stream()
			.anyMatch(user -> user.getEmail().equals("admin@ootd.com"));

		assertThat(admins).asList().isNotEmpty();
		assertThat(containsAdminEmail).isTrue();
	}

	@Test
	void 유저저장시_기본필드가_설정된다() {
		// given
		User user = new User();
		user.setEmail("audit@ootd.com");
		user.setName("감사");
		user.setPasswordHash("hashed");
		user.setRole(Role.USER);
		user.setLocked(false);

		// when
		User saved = userRepository.save(user);

		// then
		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getCreatedAt()).isNotNull();
	}

	@Test
	void 유저와_프로필_연관관계_확인() {
		// given
		User user = new User();
		user.setEmail("linked@ootd.com");
		user.setName("연결됨");
		user.setPasswordHash("hashed");
		user.setRole(Role.USER);
		user.setLocked(false);
		userRepository.save(user);

		Profile profile = new Profile(user, "닉네임", Gender.MALE);
		user.setProfile(profile);
		profileRepository.save(profile);

		// when
		User found = userRepository.findById(user.getId()).orElseThrow();

		// then
		assertThat(found.getProfile()).isNotNull();
		assertThat(found.getProfile().getNickname()).isEqualTo("닉네임");
	}

	@Test
	@DisplayName("잠기지 않은 사용자만 조회")
	void 잠기지_않은_사용자만_조회() {
		// given
		User lockedUser = new User();
		lockedUser.setEmail("locked@ootd.com");
		lockedUser.setName("잠김");
		lockedUser.setPasswordHash("hashed");
		lockedUser.setRole(Role.USER);
		lockedUser.setLocked(true);
		userRepository.save(lockedUser);

		User activeUser = new User();
		activeUser.setEmail("active@ootd.com");
		activeUser.setName("활성");
		activeUser.setPasswordHash("hashed");
		activeUser.setRole(Role.USER);
		activeUser.setLocked(false);
		userRepository.save(activeUser);

		// when
		List<User> result = userRepository.findByLockedFalse();

		// then
		assertThat(result).extracting("email").contains("active@ootd.com");
		assertThat(result).extracting("email").doesNotContain("locked@ootd.com");
	}

	@Test
	@DisplayName("이메일에 특정 키워드가 포함된 사용자 검색")
	void 이메일_키워드로_검색() {
		// given
		User user1 = new User();
		user1.setEmail("alice@example.com");
		user1.setName("앨리스");
		user1.setPasswordHash("pw");
		user1.setRole(Role.USER);
		user1.setLocked(false);
		userRepository.save(user1);

		User user2 = new User();
		user2.setEmail("bob@example.com");
		user2.setName("밥");
		user2.setPasswordHash("pw");
		user2.setRole(Role.USER);
		user2.setLocked(false);
		userRepository.save(user2);

		// when
		List<User> result = userRepository.findByEmailContaining("alice");

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getEmail()).isEqualTo("alice@example.com");
	}

	@Test
	@DisplayName("가입일 순으로 정렬된 사용자 리스트 조회")
	void 가입일_순_정렬() throws InterruptedException {
		// given
		User early = new User();
		early.setEmail("old@ootd.com");
		early.setName("초기");
		early.setPasswordHash("pw");
		early.setRole(Role.USER);
		early.setLocked(false);
		userRepository.save(early);

		Thread.sleep(100); // createdAt 차이 보장

		User recent = new User();
		recent.setEmail("new@ootd.com");
		recent.setName("최근");
		recent.setPasswordHash("pw");
		recent.setRole(Role.USER);
		recent.setLocked(false);
		userRepository.save(recent);

		// when
		List<User> result = userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

		// then
		assertThat(result.get(0).getEmail()).isEqualTo("new@ootd.com");
	}
}

@TestConfiguration
class UserRepositoryTestConfig {

	@Bean
	public JPAQueryFactory jpaQueryFactory(EntityManager em) {
		return new JPAQueryFactory(em);
	}
}
