package com.nhnacademy.gateway.jwt.util;

import java.nio.charset.StandardCharsets;
import java.security.Key;

import org.springframework.stereotype.Service;

import com.nhnacademy.gateway.jwt.status.TokenStatus;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

/**
 * JWT 토큰을 활용하는데 필요한 메서드들을 한 데 모아놓은 클래스
 */
@Service
@RequiredArgsConstructor
public class JwtUtil {

	/**
	 * 검사하고자 하는 Token 과 SecretKey 를 전달받아, 해당 토큰의 유효 기간이 지나지 않았고 유효한지 여부를 파악한다
	 */
	public TokenStatus getTokenStatus(String token, Key secretKey) {
		try {
			if (token == null || token.trim().isEmpty()) {
				return TokenStatus.INVALID;
			}

			Jwts.parserBuilder()
				.setSigningKey(secretKey)
				.build()
				.parseClaimsJws(token); // 여기에서 파싱이 되면 유효한 토큰이다

			return TokenStatus.AUTHENTICATED;
		} catch (ExpiredJwtException e) {
			return TokenStatus.EXPIRED;
		} catch (JwtException e) {
			throw new JwtException("유효하지 않은 토큰입니다.");
		}
	}


	/**
	 * 특정 토큰의 시크릿 키로 사용하고자할 때, Keys 클래스의 정적 메서드를 이용하여 Key 객체를 반환한다
	 */
	public Key getSigningKey(String secretKey) {
		return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
	}

}
