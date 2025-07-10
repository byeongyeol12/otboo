package com.codeit.otboo.domain.follow.repository;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import com.codeit.otboo.domain.follow.entity.Follow;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.global.config.QueryDslConfig;
import com.codeit.otboo.global.enumType.Role;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DataJpaTest
@Import(QueryDslConfig.class)
@EnableJpaAuditing
public class FollowRepositoryTest {
	@Autowired
	private FollowRepository followRepository;
	@Autowired
	private UserRepository userRepository;

	User follower;
	User followee;
	User anotherUser;

	@BeforeEach
	public void setUp() {
		// 유저 더미 생성 및 저장
		follower = new User();
		follower.setEmail("follower@example.com");
		follower.setName("팔로워");
		follower.setPasswordHash("pw1");
		follower.setRole(Role.USER);
		follower.setField("IT");
		userRepository.save(follower);

		followee = new User();
		followee.setEmail("followee@example.com");
		followee.setName("팔로이");
		followee.setPasswordHash("pw2");
		followee.setRole(Role.USER);
		followee.setField("MKT");
		userRepository.save(followee);

		anotherUser = new User();
		anotherUser.setEmail("another@example.com");
		anotherUser.setName("유저");
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

}
