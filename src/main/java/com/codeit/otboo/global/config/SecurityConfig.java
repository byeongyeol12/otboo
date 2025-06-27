package com.codeit.otboo.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf->csrf.disable())
			.authorizeHttpRequests(auth -> auth
				.anyRequest().permitAll() // 인증 없이 모두 허용
			);
		return http.build();
	}
}