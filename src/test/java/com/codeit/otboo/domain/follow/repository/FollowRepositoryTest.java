package com.codeit.otboo.domain.follow.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

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
	@DisplayName("findFollowees - 유저가 팔로우하는 사람 목록(커서 x)")
	void findFollowees_noCursor(){
		//given
		Follow follow = Follow.builder().follower(follower).followee(followee).build();
		Follow follow2 = Follow.builder().follower(follower).followee(anotherUser).build();
		followRepository.save(follow);
		followRepository.save(follow2);
		Pageable pageable = PageRequest.of(0, 10);

		//when
		List<Follow> followees = followRepository.findFollowees(follower.getId(),null,null,pageable);

		//then
		assertThat(followees).hasSize(2);
		//리스트에서 각 팔로우 객체의 "팔로이" ID만 추출해서,
		// 그 안에 내가 팔로우했던 2명(followee, anotherUser)가 모두 포함됐는지 검증
		assertThat(followees).extracting(f -> f.getFollowee().getId())
			.contains(followee.getId(), anotherUser.getId());
	}

	@Test
	@DisplayName("findFollowees - 유저가 팔로우하는 사람 목록(커서 x, idAfter o, nameLike o)")
	void findFollowees_noCursor_usedIdAfter_usedNameLike() {
		//given
		Follow follow1 = Follow.builder().follower(follower).followee(followee).build();
		Follow follow2 = Follow.builder().follower(follower).followee(anotherUser).build();
		followRepository.save(follow1);
		followRepository.save(follow2);
		Pageable pageable = PageRequest.of(0, 10);

		// when: nameLike 필터
		List<Follow> byName = followRepository.findFollowees(
			follower.getId(), null, "팔", pageable);

		// then
		assertThat(byName).hasSize(1);
		assertThat(byName.get(0).getFollowee().getName()).isEqualTo("팔로이");


		// when: idAfter 필터
		List<Follow> after = followRepository.findFollowees(
			follower.getId(), follow1.getId(), null, pageable);

		// then
		assertThat(after).hasSize(1);
		assertThat(after.get(0).getId()).isEqualTo(follow2.getId());
	}

	@Test
	@DisplayName("findFollowees - 유저가 팔로우하는 사람 목록(커서 x, idAfter o, nameLike o)")
	void findFollowees_withCursorAndNameLike() {
		//given
		Follow follow1 = Follow.builder().follower(follower).followee(followee).build();
		Follow follow2 = Follow.builder().follower(follower).followee(anotherUser).build();
		followRepository.save(follow1);
		followRepository.save(follow2);
		Pageable pageable = PageRequest.of(0, 10);

		//when
		List<Follow> result = followRepository.findFollowees(follower.getId(),follow1.getId(),"팔",pageable);

		//then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getFollowee().getName()).isEqualTo("팔로");
	}

	// findFollowers
	@Test
	@DisplayName("findFollowers - 유저를 팔로우하는 사람 목록(커서 x)")
	void findFollowers_noCursor() {
		//given
		Follow follow = Follow.builder().follower(follower).followee(followee).build();
		Follow follow2 = Follow.builder().follower(anotherUser).followee(followee).build();
		followRepository.save(follow);
		followRepository.save(follow2);
		Pageable pageable = PageRequest.of(0, 10);

		//when
		List<Follow> followers = followRepository.findFollowers(followee.getId(), null, null, pageable);

		//then
		assertThat(followers).hasSize(2);
		// 리스트에서 각 팔로우 객체의 "팔로워" ID만 추출해서,
		// 그 안에 follower, anotherUser가 모두 포함됐는지 검증
		assertThat(followers).extracting(f -> f.getFollower().getId())
			.contains(follower.getId(), anotherUser.getId());
	}

	@Test
	@DisplayName("findFollowers - 유저를 팔로우하는 사람 목록(커서 x, idAfter o, nameLike o)")
	void findFollowers_noCursor_usedIdAfter_usedNameLike() {
		//given
		Follow follow1 = Follow.builder().follower(follower).followee(followee).build();
		Follow follow2 = Follow.builder().follower(anotherUser).followee(followee).build();
		followRepository.save(follow1);
		followRepository.save(follow2);
		Pageable pageable = PageRequest.of(0, 10);

		// when
		List<Follow> byName = followRepository.findFollowers(
			followee.getId(), null, "팔로워", pageable);

		// then
		assertThat(byName).hasSize(1);
		assertThat(byName.get(0).getFollower().getName()).isEqualTo("팔로워");

		// when: idAfter 필터
		List<Follow> after = followRepository.findFollowers(
			followee.getId(), follow1.getId(), null, pageable);

		// then
		assertThat(after).hasSize(1);
		assertThat(after.get(0).getId()).isEqualTo(follow2.getId());
	}

	@Test
	@DisplayName("findFollowers - 유저를 팔로우하는 사람 목록(커서 x, idAfter o, nameLike o, 둘 다)")
	void findFollowers_withCursorAndNameLike() {
		//given
		Follow follow1 = Follow.builder().follower(follower).followee(followee).build();
		Follow follow2 = Follow.builder().follower(anotherUser).followee(followee).build();
		followRepository.save(follow1);
		followRepository.save(follow2);
		Pageable pageable = PageRequest.of(0, 10);

		//when
		List<Follow> result = followRepository.findFollowers(
			followee.getId(), follow1.getId(), "팔로워", pageable);

		//then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getFollower().getName()).isEqualTo("팔로워");
	}
}
