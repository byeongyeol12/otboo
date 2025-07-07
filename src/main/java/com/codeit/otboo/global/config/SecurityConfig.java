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
			.csrf(csrf -> csrf
				.csrfTokenRepository(customCsrfTokenRepository())
				.ignoringRequestMatchers(request -> {
					String path = request.getRequestURI();
					String method = request.getMethod();
					// 로그인 요청만 제외
					return (
						(path.equals("/api/auth/sign-in") ||
							path.equals("/api/auth/sign-out") ||  // ✅ 로그아웃 추가
							path.equals("/api/users")) && method.equalsIgnoreCase("POST")
					) || (
						path.matches("^/api/users/.+/profiles$") && method.equalsIgnoreCase("PATCH")
					) || (
						path.matches("^/api/users/.+/role$") && method.equalsIgnoreCase("PATCH")
					);
				})
			)

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
					"/h2-console/**"
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
