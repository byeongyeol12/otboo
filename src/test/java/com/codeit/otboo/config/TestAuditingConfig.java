package com.codeit.otboo.config;

import static org.mockito.Mockito.*;

import java.util.Optional;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;

import com.codeit.otboo.domain.auth.service.JwtBlacklistService;

@TestConfiguration
public class TestAuditingConfig {

	@Bean
	public AuditorAware<String> auditorAware() {
		return () -> Optional.of("test-user");
	}

	@Bean
	public JwtBlacklistService jwtBlacklistService() {
		return mock(JwtBlacklistService.class);
	}
}
