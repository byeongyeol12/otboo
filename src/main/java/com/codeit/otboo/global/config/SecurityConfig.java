package com.codeit.otboo.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.codeit.otboo.global.config.jwt.JwtAuthenticationFilter;
import com.codeit.otboo.global.config.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtTokenProvider jwtTokenProvider;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.cors(Customizer.withDefaults())
			.csrf(csrf -> csrf.disable()) // ✅ CSRF 완전히 끔

			.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
			)

			//  H2 콘솔 등 iframe 접근 허용
			.headers(headers ->
				headers.frameOptions(frame -> frame.sameOrigin())
			)

			//  요청 인증/인가 설정
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					"/", "/index.html", "/favicon.ico",
					"/assets/**",
					"/api/auth/**",
					"/api/weathers/**",
					"/api/batch/**",
					"/api/users/**",
					"/api/sse",
					"/h2-console/**",
					"/uploads/**",// 테스트를 위해 임시로 추가
					"/ws/**"
				).permitAll()
				.anyRequest().authenticated()
			)

			.addFilterBefore(
				new JwtAuthenticationFilter(jwtTokenProvider),
				UsernamePasswordAuthenticationFilter.class
			);

		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
		throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public CookieCsrfTokenRepository customCsrfTokenRepository() {
		CookieCsrfTokenRepository repo = CookieCsrfTokenRepository.withHttpOnlyFalse();
		repo.setCookiePath("/");
		return repo;
	}
}

