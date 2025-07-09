// package com.codeit.otboo.domain.follow.repository;
//
// import static org.assertj.core.api.Assertions.*;
//
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
// import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
// import org.springframework.context.annotation.Import;
// import org.springframework.test.context.ActiveProfiles;
//
// import com.codeit.otboo.domain.follow.entity.Follow;
// import com.codeit.otboo.domain.user.entity.User;
// import com.codeit.otboo.domain.user.repository.UserRepository;
// import com.codeit.otboo.global.config.QueryDslConfig;
// import com.codeit.otboo.global.enumType.Role;
//
// @ActiveProfiles("test")
// @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // 내장 DB로 대체하지 않음
// @DataJpaTest
// @Import(QueryDslConfig.class) // QueryDslConfig 명시적으로 포함
// public class FollowRepositoryTest {
// 	@Autowired
// 	private FollowRepository followRepository;
//
// 	@Autowired
// 	private UserRepository userRepository;
//
// 	private User userA;
// 	private User userB;
// 	private User userC;
//
// 	@BeforeEach
// 	void setUp() {
// 		userA = new User();
// 		userA.setName("A");
// 		userA.setEmail("userA@test.com");
// 		userA.setPasswordHash("pwA");
// 		userA.setRole(Role.USER);
// 		userA.setLocked(false);
//
// 		userB = new User();
// 		userB.setName("B");
// 		userB.setEmail("userB@test.com");
// 		userB.setPasswordHash("pwB");
// 		userB.setRole(Role.USER);
// 		userB.setLocked(false);
//
// 		userC = new User();
// 		userC.setName("C");
// 		userC.setEmail("userC@test.com");
// 		userC.setPasswordHash("pwC");
// 		userC.setRole(Role.USER);
// 		userC.setLocked(false);
// 	}
//
// 	//existsByFollowerIdAndFolloweeId() : 중복 팔로우 방지 체크
// 	@Test
// 	@DisplayName("중복 팔로우 존재 여부 확인")
// 	void existsByFollowerIdAndFolloweeId_ReturnTrue() {
// 		//given
// 		userRepository.save(userA);
// 		userRepository.save(userB);
//
// 		Follow follow = new Follow(userA, userB);
// 		followRepository.save(follow);
//
// 		//when
// 		boolean exists = followRepository.existsByFollowerIdAndFolloweeId(userA.getId(), userB.getId());
//
// 		//then
// 		assertThat(exists).isTrue();
// 	}
// }
