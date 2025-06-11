package com.nhnacademy.gateway.jwt.util;

import java.security.Key;
import java.util.Date;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.nhnacademy.gateway.jwt.status.TokenStatus;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

class JwtUtilTest {

	private JwtUtil jwtUtil;
	private String secret;
	private Key signingKey;

	@BeforeEach
	void setUp() {
		jwtUtil = new JwtUtil();
		secret = "secret-secret-secret-secret-secret";
		signingKey = jwtUtil.getSigningKey(secret);
	}

	private String createJwt(long expirationMillisOffset) {
		return Jwts.builder()
			.setSubject("user")
			.claim("MemberId", "user")
			.setIssuedAt(new Date())
			.setExpiration(new Date(System.currentTimeMillis() + expirationMillisOffset))
			.signWith(signingKey, SignatureAlgorithm.HS256)
			.compact();
	}

	@Test
	@DisplayName("유효한 토큰 상태 반환 확인")
	void testValidTokenReturnsAuthenticated() {

		// Given
		String token = createJwt(60_000);

		// When
		TokenStatus status = jwtUtil.getTokenStatus(token, signingKey);

		// Then
		Assertions.assertThat(status).isEqualTo(TokenStatus.AUTHENTICATED);

	}

	@Test
	@DisplayName("만료된 토큰 상태 반환 확인")
	void testExpiredTokenReturnsExpired() {

		// Given
		String token = createJwt(-60_000);

		// When
		TokenStatus status = jwtUtil.getTokenStatus(token, signingKey);

		// Then
		Assertions.assertThat(status).isEqualTo(TokenStatus.EXPIRED);

	}

	@Test
	@DisplayName("빈 토큰 상태 반환 확인")
	void testEmptyTokenReturnsInvalid() {

		// Given

		// When
		TokenStatus status = jwtUtil.getTokenStatus("   ", signingKey);

		// Then
		Assertions.assertThat(status).isEqualTo(TokenStatus.INVALID);

	}

	@Test
	@DisplayName("null 토큰 상태 반환 확인")
	void testNullTokenReturnsInvalid() {

		// Given

		// When
		TokenStatus status = jwtUtil.getTokenStatus(null, signingKey);

		// Then
		Assertions.assertThat(status).isEqualTo(TokenStatus.INVALID);

	}

	@Test
	@DisplayName("Secret 키 생성 정상 동작 확인")
	void testGetSigningKey() {

		// Given

		// When
		Key key = jwtUtil.getSigningKey(secret);

		// Then
		Assertions.assertThat(key).isNotNull();
		Assertions.assertThat(key.getAlgorithm()).isEqualTo("HmacSHA256");

	}

}