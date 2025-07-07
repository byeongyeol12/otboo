package com.codeit.otboo.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // Spring Security 설정을 활성화합니다.
public class SecurityConfig {

	@Bean // 이 메소드가 반환하는 객체를 Spring 컨테이너에 Bean으로 등록합니다.
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			// 1. CSRF(Cross-Site Request Forgery) 보호 기능을 비활성화합니다.
			// REST API는 상태를 저장하지 않으므로(stateless), 일반적으로 CSRF 보호가 필요 없습니다.
			.csrf(csrf -> csrf.disable())

			// 2. HTTP 요청에 대한 접근 권한을 설정합니다.
			.authorizeHttpRequests(auth -> auth
				// "/api/weathers/**" 경로의 모든 요청은 인증 없이 허용합니다.
				// '/**'는 하위 모든 경로를 포함한다는 의미입니다.
				.requestMatchers("/api/weathers/**").permitAll()
				.requestMatchers("/api/batch/**").permitAll() // 배치 실행 API 허용

				// 그 외 나머지 모든 요청은 반드시 인증(로그인)이 필요합니다.
				.anyRequest().authenticated()
			);

		return http.build();
	}
}