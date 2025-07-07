package com.codeit.otboo.domain.auth.service;

import java.util.UUID;

public interface TokenCacheService {
	void saveRefreshToken(UUID userId, String refreshToken);

	void invalidateRefreshToken(UUID userId);

	boolean isRefreshTokenValid(UUID userId, String refreshToken);
}
