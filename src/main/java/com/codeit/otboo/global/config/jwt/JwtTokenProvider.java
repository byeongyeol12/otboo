package com.codeit.otboo.global.config.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.codeit.otboo.domain.auth.service.JwtBlacklistService;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.error.ErrorCode;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtTokenProvider {

	private final Key secretKey;
	private final long tokenValidityInMilliseconds;

	@Value("${jwt.refresh-token-validity-in-ms}")
	private long refreshTokenValidityInMilliseconds;

	private final JwtBlacklistService blacklistService;

	public JwtTokenProvider(
		@Value("${jwt.secret}") String secret,
		@Value("${jwt.expiration}") long tokenValidityInMilliseconds,
		JwtBlacklistService blacklistService
	) {
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.tokenValidityInMilliseconds = tokenValidityInMilliseconds;
		this.blacklistService = blacklistService;
	}

	public String generateToken(UUID userId, String email, String name, String role) {
		Instant now = Instant.now();
		Instant expiry = now.plusMillis(tokenValidityInMilliseconds);

		return Jwts.builder()
			.setSubject(email) // 보통 유저 식별용
			.claim("userId", userId.toString())
			.claim("email", email)
			.claim("name", name)
			.claim("role", role)
			.setIssuedAt(Date.from(now))
			.setExpiration(Date.from(expiry))
			.signWith(secretKey, SignatureAlgorithm.HS256)
			.compact();
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder()
				.setSigningKey(secretKey)
				.build()
				.parseClaimsJws(token);
			return true;
		} catch (ExpiredJwtException e) {
			throw new CustomException(ErrorCode.EXPIRED_TOKEN);
		} catch (UnsupportedJwtException | MalformedJwtException e) {
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		} catch (IllegalArgumentException e) {
			throw new CustomException(ErrorCode.UNAUTHORIZED);
		}
	}

	public Claims getClaims(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(secretKey)
			.build()
			.parseClaimsJws(token)
			.getBody();
	}

	public long getTokenValidityInMilliseconds() {
		return tokenValidityInMilliseconds;
	}

	public String resolveToken(HttpServletRequest request) {
		String bearer = request.getHeader("Authorization");
		if (bearer != null && bearer.startsWith("Bearer ")) {
			return bearer.substring(7);
		}
		throw new CustomException(ErrorCode.INVALID_TOKEN);
	}

	public String getSubject(String token) {
		return getClaims(token).getSubject();
	}

	public String generateRefreshToken(UUID userId) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + refreshTokenValidityInMilliseconds); // 예: 7일

		return Jwts.builder()
			.setSubject(userId.toString())
			.setIssuedAt(now)
			.setExpiration(expiryDate)
			.signWith(secretKey, SignatureAlgorithm.HS256)
			.compact();
	}

	public void invalidateUserTokens(String accessToken) {
		long remainingMillis = getRemainingMillis(accessToken);
		blacklistService.blacklistToken(accessToken, remainingMillis);
	}

	private long getRemainingMillis(String token) {
		Claims claims = getClaims(token);
		long exp = claims.getExpiration().getTime();
		long now = System.currentTimeMillis();
		return Math.max(exp - now, 0);
	}
}