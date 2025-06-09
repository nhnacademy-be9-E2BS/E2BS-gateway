package com.nhnacademy.gateway.jwt.util;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nhnacademy.gateway.jwt.rule.JwtRule;
import com.nhnacademy.gateway.jwt.status.TokenStatus;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;

/**
 * JWT 토큰을 활용하는데 필요한 메서드들을 한 데 모아놓은 클래스
 */
@Service
@RequiredArgsConstructor
public class JwtUtil {

	/**
	 * JWT Token 에서 MemberId 값 추출하는 메서드
	 */
	public String getMemberId(String token, Key secretKey) {
		try {
			Claims claims = Jwts.parserBuilder()
				.setSigningKey(secretKey)
				.build()
				.parseClaimsJws(token)
				.getBody();

			return claims.get("MemberId", String.class);

		} catch (JwtException e) {
			return null;
		}
	}

	/**
	 * JWT Token MemberRole 값 추출하는 메서드
	 */
	public List<String> getMemberRole(String token, Key secretKey) {
		try {
			Claims claims = Jwts.parserBuilder()
				.setSigningKey(secretKey)
				.build()
				.parseClaimsJws(token)
				.getBody();

			return claims.get("Role", List.class);
		} catch (JwtException e) {
			return Collections.emptyList();
		}

	}

	/**
	 * 만료된 토큰에서 Claims 추출
	 */
	public Claims getClaimsIfExpired(String token, Key secretKey) {
		try {
			return Jwts.parserBuilder()
				.setSigningKey(secretKey)
				.build()
				.parseClaimsJws(token)
				.getBody();
		} catch (ExpiredJwtException e) {
			return e.getClaims();
		} catch (JwtException e) {
			throw new JwtException("Expired or invalid JWT token");
		}
	}

	/**
	 * 만료된 토큰에서 MemberId 추출하는 메서드
	 */
	public String getMemberIdIfExpired(String token, Key secretKey) {
		Claims claims = getClaimsIfExpired(token, secretKey);

		return claims.get("MemberId", String.class);
	}

	/**
	 * 만료된 토큰에서 MemberRole 추출하는 메서드
	 */
	public List<String> getMemberRoleIfExpired(String token, Key secretKey) {
		Claims claims = getClaimsIfExpired(token, secretKey);

		return claims.get("Role", List.class);
	}

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
	 * Cookie 에서 원하는 토큰을 찾는 역할
	 * 쿠키의 제목 값을 통해 원하는 쿠키를 찾는 메서드
	 */
	public String resolveTokenFromCookie(Cookie[] cookies, JwtRule tokenPrefix) {
		return Arrays.stream(cookies)
			.filter(cookie -> cookie.getName().equals(tokenPrefix.getValue()))
			.findFirst()
			.map(Cookie::getValue)
			.orElse("");
	}

	/**
	 * 특정 토큰의 시크릿 키로 사용하고자할 때, Keys 클래스의 정적 메서드를 이용하여 Key 객체를 반환한다
	 */
	public Key getSigningKey(String secretKey) {
		return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * 이진 데이터를 ASCII 문자열로 안전하게 인코딩
	 */
	public String encodeToBase64(String secretKey) {
		return Base64.getEncoder().encodeToString(secretKey.getBytes());
	}

	/**
	 * Cookie 에서 원하는 토큰을 reset 하는 기능
	 */
	public Cookie resetToken(JwtRule tokenPrefix) {
		Cookie cookie = new Cookie(tokenPrefix.getValue(), null);
		cookie.setMaxAge(0);
		cookie.setPath("/");
		cookie.setSecure(true);
		cookie.setHttpOnly(true);

		return cookie;
	}

}
