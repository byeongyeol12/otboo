package com.codeit.otboo.global.config;

import com.codeit.otboo.global.config.jwt.JwtAuthenticationFilter;
import com.codeit.otboo.global.config.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtTokenProvider jwtTokenProvider;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				// 1. CSRF, Form Login, HTTP Basic 등 불필요한 기능 비활성화
				.csrf(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)

				// 2. JWT 인증을 사용하므로 세션을 STATELESS(상태 없음)로 설정
				.sessionManagement(session ->
						session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				)

				// 3. 요청별 인증/인가 설정
				.authorizeHttpRequests(auth -> auth
						// Swagger UI, H2 콘솔 등 개발 편의를 위한 경로는 모두 허용
						.requestMatchers(
								"/", "/index.html", "/favicon.ico", "/assets/**",
								"/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
								"/h2-console/**", "/uploads/**", "/ws/**", "/api/sse"
						).permitAll()

						// 회원가입 및 인증 관련 API는 모두 허용
						.requestMatchers("/api/auth/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/users").permitAll() // POST /api/users (회원가입)

						// ✨ 배치 실행 API는 로그인한 사용자라면 누구나 접근 가능하도록 허용
						.requestMatchers("/api/batch/**").authenticated()

						// 그 외 모든 요청은 인증된 사용자만 접근 가능
						.anyRequest().authenticated()
				)

				// 4. JWT 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
				.addFilterBefore(
						new JwtAuthenticationFilter(jwtTokenProvider),
						UsernamePasswordAuthenticationFilter.class
				);

		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}