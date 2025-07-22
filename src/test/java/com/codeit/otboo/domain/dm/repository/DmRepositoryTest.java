// package com.codeit.otboo.domain.dm.repository;
//
// import static org.assertj.core.api.Assertions.*;
//
// import java.util.List;
//
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
// import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
// import org.springframework.context.annotation.Import;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
// import org.springframework.test.context.ActiveProfiles;
//
// import com.codeit.otboo.domain.dm.entity.Dm;
// import com.codeit.otboo.domain.user.entity.User;
// import com.codeit.otboo.domain.user.repository.UserRepository;
// import com.codeit.otboo.global.config.QueryDslConfig;
// import com.codeit.otboo.global.enumType.Role;
//
// @ActiveProfiles("test")
// @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// @DataJpaTest
// @Import(QueryDslConfig.class)
// @EnableJpaAuditing
// public class DmRepositoryTest {
//
// 	@Autowired
// 	private DmRepository dmRepository;
//
// 	@Autowired
// 	private UserRepository userRepository;
//
// 	private User sender,receiver;
//
// 	@BeforeEach
// 	void setUp() {
// 		sender = new User();
// 		sender.setName("sender");
// 		sender.setEmail("sender@email.com");
// 		sender.setPasswordHash("pw1");
// 		sender.setRole(Role.USER);
// 		sender.setField("T1");
// 		userRepository.save(sender);
//
// 		receiver = new User();
// 		receiver.setName("receiver");
// 		receiver.setEmail("receiver@email.com");
// 		receiver.setPasswordHash("pw2");
// 		receiver.setRole(Role.USER);
// 		receiver.setField("T2");
// 		userRepository.save(receiver);
//
// 		//DM 생성
// 		for(int i=0;i<3;i++){
// 			dmRepository.save(Dm.builder()
// 				.sender(i%2==0?sender:receiver)
// 				.receiver(i%2==0?receiver:sender)
// 				.content("test content "+i)
// 				.build());
// 		}
// 	}
//
// 	//findAllByUserIdAndOtherIdAfterCursor
// 	@Test
// 	@DisplayName("findAllByUserIdAndOtherIdAfterCursor - 모든 메시지 createdAt,오름차순 조회")
// 	void findAllByUserIdAndOtherIdAfterCursor_createdAt_ASC() {
// 		//given
// 		Pageable pageable = PageRequest.of(0, 10);
//
// 		//when
// 		List<Dm> result = dmRepository.findAllByUserIdAndOtherIdAfterCursor(sender.getId(), receiver.getId(), null,pageable);
//
// 		//then
// 		assertThat(result).hasSize(3);
// 		assertThat(result.get(0).getCreatedAt()).isBefore(result.get(1).getCreatedAt());
// 		assertThat(result.get(1).getCreatedAt()).isBefore(result.get(2).getCreatedAt());
// 	}
// }
