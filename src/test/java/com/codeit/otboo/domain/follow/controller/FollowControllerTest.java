// package com.codeit.otboo.domain.follow.controller;
//
// import static org.mockito.BDDMockito.*;
// import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
// import java.util.UUID;
//
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
// import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
// import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
// import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.http.MediaType;
// import org.springframework.security.authentication.TestingAuthenticationToken;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.transaction.PlatformTransactionManager;
//
// import com.codeit.otboo.domain.follow.dto.FollowCreateRequest;
// import com.codeit.otboo.domain.follow.dto.FollowDto;
// import com.codeit.otboo.domain.follow.service.FollowService;
// import com.codeit.otboo.domain.user.dto.response.UserSummaryDto;
// import com.codeit.otboo.global.config.security.UserPrincipal;
// import com.codeit.otboo.global.enumType.Role;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.springframework.boot.autoconfigure.data.jpa.DataJpaAutoConfiguration;
//
// import jakarta.persistence.EntityManagerFactory;
//
// @WebMvcTest(
// 	controllers = FollowController.class,
// 	excludeAutoConfiguration = {
// 		DataSourceAutoConfiguration.class,
// 		DataSourceTransactionManagerAutoConfiguration.class,
// 		HibernateJpaAutoConfiguration.class,
// 		JpaRepositoriesAutoConfiguration.class,
// 		DataJpaAutoConfiguration.class    // ← 여기에 추가
// 	}
// )
// @ActiveProfiles("test")
// @AutoConfigureMockMvc(addFilters = false)
// class FollowControllerTest {
//
// 	@Autowired
// 	private MockMvc mockMvc;
//
// 	@Autowired
// 	private ObjectMapper objectMapper;
//
// 	@MockBean
// 	private FollowService followService;
//
// 	private UserPrincipal userPrincipal;
//
// 	@BeforeEach
// 	void setUp() {
// 		userPrincipal = new UserPrincipal(
// 			UUID.randomUUID(),
// 			"test@example.com",
// 			"testuser",
// 			Role.USER
// 		);
// 	}
//
// 	@Test
// 	@DisplayName("POST /api/follows - 팔로우 생성 성공")
// 	void createFollow_success() throws Exception {
// 		UUID followeeId = UUID.randomUUID();
// 		FollowCreateRequest request = new FollowCreateRequest(followeeId, userPrincipal.getId());
// 		FollowDto responseDto = new FollowDto(
// 			UUID.randomUUID(),
// 			new UserSummaryDto(followeeId, "followeeName", null),
// 			new UserSummaryDto(userPrincipal.getId(), "followerName", null)
// 		);
//
// 		given(followService.createFollow(userPrincipal.getId(), followeeId))
// 			.willReturn(responseDto);
//
// 		mockMvc.perform(post("/api/follows")
// 				.with(authentication(new TestingAuthenticationToken(userPrincipal, null)))
// 				.with(csrf())
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(request)))
// 			.andExpect(status().isCreated())
// 			.andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
//
// 		then(followService).should().createFollow(userPrincipal.getId(), followeeId);
// 	}
// }
