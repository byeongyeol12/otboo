package com.codeit.otboo.domain.follow.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.codeit.otboo.domain.follow.entity.Follow;
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
public class FollowRepositoryTest {
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

		// registry.add("spring.data.redis.host", redis::getHost);
		// registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
	}

	@Autowired
	private FollowRepository followRepository;
	@Autowired
	private UserRepository userRepository;

	User follower;
	User followee;
	User anotherUser;

	static final UUID FOLLOW_ID_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
	static final UUID FOLLOW_ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
	static final UUID FOLLOW_ID_3 = UUID.fromString("00000000-0000-0000-0000-000000000003");


	@BeforeEach
	public void setUp() {
		// 유저 더미 생성 및 저장
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

		anotherUser = new User();
		anotherUser.setEmail("another@example.com");
		anotherUser.setName("유저3");
		anotherUser.setPasswordHash("pw3");
		anotherUser.setRole(Role.USER);
		anotherUser.setField("SALES");
		userRepository.save(anotherUser);
	}

	//existsByFollowerIdAndFolloweeId
	@Test
	@DisplayName("existsByFollowerIdAndFolloweeId - 중복 팔로우 방지 성공")
	void existsByFollowerIdAndFolloweeId_success() {
		//given
		Follow follow = Follow.builder().follower(follower).followee(followee).build();
		followRepository.save(follow);

		//when
		boolean exist = followRepository.existsByFollowerIdAndFolloweeId(follower.getId(), followee.getId());

		//then
		assertThat(exist).isTrue();
	}

	//findFollowees
	@Test
	@DisplayName("findFollowees - idAfterX, nameLikeX, limitO(10), sortBy='id', DESC")
	void findFollowees_noIdAfterAndNameLike_usedLimitAndSortByIdDESC(){
		// given
		Follow f1 = Follow.builder().id(FOLLOW_ID_1).follower(follower).followee(followee).build();
		Follow f2 = Follow.builder().id(FOLLOW_ID_2).follower(follower).followee(anotherUser).build();
		followRepository.save(f1);
		followRepository.save(f2);
		Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC,"id"));

		// when
		List<Follow> result = followRepository.findFollowees(follower.getId(),null,null,pageable);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(1).getId()).isEqualTo(FOLLOW_ID_1);
		assertThat(result.get(0).getId()).isEqualTo(FOLLOW_ID_2);
		assertThat(result.get(0).getFollowee().getName()).isIn("유저2", "유저3");
	}

	@Test
	@DisplayName("findFollowees - idAfterX, nameLikeX, limitO(10), sortBy='id', ASC")
	void findFollowees_noIdAfterAndNameLike_usedLimitAndSortByIdASC(){
		// given
		Follow f1 = Follow.builder().id(FOLLOW_ID_1).follower(follower).followee(followee).build();
		Follow f2 = Follow.builder().id(FOLLOW_ID_2).follower(follower).followee(anotherUser).build();
		followRepository.save(f1);
		followRepository.save(f2);
		Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC,"id"));

		// when
		List<Follow> result = followRepository.findFollowees(follower.getId(),null,null,pageable);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getId()).isEqualTo(FOLLOW_ID_1);
		assertThat(result.get(1).getId()).isEqualTo(FOLLOW_ID_2);
		assertThat(result.get(0).getFollowee().getName()).isIn("유저2", "유저3");
	}

	@Test
	@DisplayName("findFollowees - idAfterO, nameLikeO, limitO(10), sortBy='id', ASC: idAfter 커서 이후(오름차순)")
	void findFollowees_usedIdAfterNameLikeLimitAndSortByIdASC() {
		// given
		Follow f1 = Follow.builder().id(FOLLOW_ID_1).follower(follower).followee(followee).build();
		Follow f2 = Follow.builder().id(FOLLOW_ID_2).follower(follower).followee(anotherUser).build();
		followRepository.save(f1);
		followRepository.save(f2);

		Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));

		// when
		List<Follow> result = followRepository.findFollowees(follower.getId(),f1.getId(),"유저",pageable);

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getFollowee().getName()).isEqualTo("유저3");
	}

	@Test
	@DisplayName("findFollowees - idAfterx, nameLikeO, limitO(10), sortBy='followee.name', ASC: idAfter 커서 이후(오름차순)")
	void findFollowees_noIdAfter_usedNameLikeLimitAndSortByNameASC() {
		// given
		Follow f1 = Follow.builder().id(FOLLOW_ID_1).follower(follower).followee(followee).build();
		Follow f2 = Follow.builder().id(FOLLOW_ID_2).follower(follower).followee(anotherUser).build();
		followRepository.save(f1);
		followRepository.save(f2);

		Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "followee.name"));

		// when
		List<Follow> result = followRepository.findFollowees(follower.getId(),null,"유저",pageable);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getFollowee().getName()).isEqualTo("유저2");
	}

	// findFollowers
	@Test
	@DisplayName("findFollowers - idAfterX, nameLikeX, limitO(10), sortBy='id', DESC")
	void findFollowers_noIdAfterAndNameLike_usedLimitAndSortByIdDESC(){
		// given
		Follow f1 = Follow.builder().id(FOLLOW_ID_1).follower(follower).followee(followee).build();
		Follow f2 = Follow.builder().id(FOLLOW_ID_2).follower(anotherUser).followee(followee).build();
		followRepository.save(f1);
		followRepository.save(f2);
		Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC,"id"));

		// when
		List<Follow> result = followRepository.findFollowers(followee.getId(), null, null, pageable);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(1).getId()).isEqualTo(FOLLOW_ID_1);
		assertThat(result.get(0).getId()).isEqualTo(FOLLOW_ID_2);
		assertThat(result.get(0).getFollower().getName()).isIn("유저1", "유저3");
	}

	@Test
	@DisplayName("findFollowers - idAfterX, nameLikeX, limitO(10), sortBy='id', ASC")
	void findFollowers_noIdAfterAndNameLike_usedLimitAndSortByIdASC(){
		// given
		Follow f1 = Follow.builder().id(FOLLOW_ID_1).follower(follower).followee(followee).build();
		Follow f2 = Follow.builder().id(FOLLOW_ID_2).follower(anotherUser).followee(followee).build();
		followRepository.save(f1);
		followRepository.save(f2);
		Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC,"id"));

		// when
		List<Follow> result = followRepository.findFollowers(followee.getId(), null, null, pageable);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getId()).isEqualTo(FOLLOW_ID_1);
		assertThat(result.get(1).getId()).isEqualTo(FOLLOW_ID_2);
		assertThat(result.get(0).getFollower().getName()).isIn("유저1", "유저3");
	}

	@Test
	@DisplayName("findFollowers - idAfterO, nameLikeO, limitO(10), sortBy='id', ASC: idAfter 커서 이후(오름차순)")
	void findFollowers_usedIdAfterNameLikeLimitAndSortByIdASC() {
		// given
		Follow f1 = Follow.builder().id(FOLLOW_ID_1).follower(follower).followee(followee).build();
		Follow f2 = Follow.builder().id(FOLLOW_ID_2).follower(anotherUser).followee(followee).build();
		followRepository.save(f1);
		followRepository.save(f2);

		Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));

		// when
		List<Follow> result = followRepository.findFollowers(followee.getId(), f1.getId(), "유저", pageable);

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getFollower().getName()).isEqualTo("유저3");
	}

	@Test
	@DisplayName("findFollowers - idAfterX, nameLikeO, limitO(10), sortBy='follower.name', ASC: 이름 기준 오름차순")
	void findFollowers_noIdAfter_usedNameLikeLimitAndSortByNameASC() {
		// given
		Follow f1 = Follow.builder().id(FOLLOW_ID_1).follower(follower).followee(followee).build();
		Follow f2 = Follow.builder().id(FOLLOW_ID_2).follower(anotherUser).followee(followee).build();
		followRepository.save(f1);
		followRepository.save(f2);

		Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "follower.name"));

		// when
		List<Follow> result = followRepository.findFollowers(followee.getId(), null, "유저", pageable);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getFollower().getName()).isEqualTo("유저1");
		assertThat(result.get(1).getFollower().getName()).isEqualTo("유저3");
	}

	//countByFollowerId
	@Test
	@DisplayName("countByFollowerId - 유저가 팔로우 하는 사람 수")
	void countByFollowerId_success(){
		//given
		Follow f1 = Follow.builder().id(FOLLOW_ID_1).follower(follower).followee(followee).build();
		Follow f2 = Follow.builder().id(FOLLOW_ID_2).follower(follower).followee(anotherUser).build();
		followRepository.save(f1);
		followRepository.save(f2);

		//when
		Long count = followRepository.countByFollowerId(follower.getId());

		//then
		assertThat(count).isEqualTo(2);
	}

	//countByFolloweeId
	@Test
	@DisplayName("countByFolloweeId - 유저를 팔로우 하는 사람 수")
	void countByFolloweeId_success(){
		//given
		Follow f1 = Follow.builder().id(FOLLOW_ID_1).follower(follower).followee(followee).build();
		Follow f2 = Follow.builder().id(FOLLOW_ID_2).follower(anotherUser).followee(followee).build();
		followRepository.save(f1);
		followRepository.save(f2);

		//when
		Long count = followRepository.countByFolloweeId(followee.getId());

		//then
		assertThat(count).isEqualTo(2);
	}

	//findByFollowerAndFollowee
	@Test
	@DisplayName("findByFollowerAndFollowee - 팔로우 조회")
	void findByFollowerAndFollowee_success(){
		//given
		Follow f1 = Follow.builder().id(FOLLOW_ID_1).follower(follower).followee(followee).build();
		Follow f2 = Follow.builder().id(FOLLOW_ID_2).follower(follower).followee(anotherUser).build();
		followRepository.save(f1);
		followRepository.save(f2);

		//when
		Optional<Follow> follow = followRepository.findByFollowerAndFollowee(follower,followee);

		//then
		assertThat(follow.isPresent()).isTrue();
		assertThat(follow.get().getId()).isEqualTo(FOLLOW_ID_1);
	}

	//existsByFollowerAndFollowee
	@Test
	@DisplayName("existsByFollowerAndFollowee - 팔로우 존재 여부")
	void existsByFollowerAndFollowee_success(){
		//given
		Follow f1 = Follow.builder().id(FOLLOW_ID_1).follower(follower).followee(followee).build();
		followRepository.save(f1);

		//when
		Boolean result = followRepository.existsByFollowerAndFollowee(follower,followee);

		//then
		assertThat(result).isTrue();
	}
}
