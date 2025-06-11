package com.nhnacademy.gateway.common.filter;

import com.nhnacademy.gateway.jwt.properties.JwtProperties;
import com.nhnacademy.gateway.jwt.rule.JwtRule;
import com.nhnacademy.gateway.jwt.status.TokenStatus;
import com.nhnacademy.gateway.jwt.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

	private JwtAuthenticationFilter filter;
	private JwtUtil jwtUtil;
	private JwtProperties jwtProperties;
	private Key dummyKey;

	@BeforeEach
	void setUp() {
		jwtUtil = mock(JwtUtil.class);
		jwtProperties = mock(JwtProperties.class);
		filter = new JwtAuthenticationFilter(jwtUtil, jwtProperties);
		dummyKey = mock(Key.class);
	}

	@Test
	@DisplayName("/api/token/** 요청은 필터 우회")
	void testTokenPathBypass() {

		// When
		MockServerHttpRequest request = MockServerHttpRequest.get("/api/token").build();
		MockServerWebExchange exchange = MockServerWebExchange.from(request);


		// When
		GatewayFilterChain chain = mock(GatewayFilterChain.class);
		when(chain.filter(exchange)).thenReturn(Mono.empty());

		// Then
		verify(chain, times(1)).filter(exchange);

	}

	@Test
	@DisplayName("/api/auth/** 요청은 필터 우회")
	void testNonAuthPathBypass() {

		// Given
		MockServerHttpRequest request = MockServerHttpRequest.get("/api/mypage").build();
		MockServerWebExchange exchange = MockServerWebExchange.from(request);

		// When
		GatewayFilterChain chain = mock(GatewayFilterChain.class);
		when(chain.filter(exchange)).thenReturn(Mono.empty());

		// Then
		verify(chain, times(1)).filter(exchange);

	}

	@Test
	@DisplayName("JWT 인증 성공 시 필터 통과")
	void testAuthenticatedToken() {

		// When
		String token = "Bearer=valid.jwt.token";

		MockServerHttpRequest request = MockServerHttpRequest.get("/api/auth")
			.header(JwtRule.JWT_ISSUE_HEADER.getValue(), token)
			.build();
		MockServerWebExchange exchange = MockServerWebExchange.from(request);

		// When
		when(jwtProperties.getAccessSecret()).thenReturn("test-secret");
		when(jwtUtil.getSigningKey(anyString())).thenReturn(dummyKey);
		when(jwtUtil.getTokenStatus("valid.jwt.token", dummyKey)).thenReturn(TokenStatus.AUTHENTICATED);

		GatewayFilterChain chain = mock(GatewayFilterChain.class);
		when(chain.filter(exchange)).thenReturn(Mono.empty());

		// Then
		verify(chain, times(1)).filter(exchange);

	}

	@Test
	@DisplayName("JWT 인증 실패 시 401 반환")
	void testUnauthenticatedToken() {

		// Given
		String token = "Bearer=invalid.jwt.token";

		MockServerHttpRequest request = MockServerHttpRequest.get("/api/auth")
			.header(JwtRule.JWT_ISSUE_HEADER.getValue(), token)
			.build();
		MockServerWebExchange exchange = MockServerWebExchange.from(request);

		// When
		when(jwtProperties.getAccessSecret()).thenReturn("test-secret");
		when(jwtUtil.getSigningKey(anyString())).thenReturn(dummyKey);
		when(jwtUtil.getTokenStatus("invalid.jwt.token", dummyKey)).thenReturn(TokenStatus.INVALID);

		GatewayFilterChain chain = mock(GatewayFilterChain.class);

		// Then
		Mono<Void> result = filter.filter(exchange, chain).then(Mono.defer(() -> {
			assertThat(exchange.getResponse().getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.UNAUTHORIZED);
			return Mono.empty();
		}));
		result.block();
		verify(chain, times(0)).filter(exchange);

	}

	@Test
	@DisplayName("토큰이 없는 경우 UNAUTHORIZED 반환")
	void testMissingToken() {

		// Given
		MockServerHttpRequest request = MockServerHttpRequest.get("/api/auth").build();
		MockServerWebExchange exchange = MockServerWebExchange.from(request);

		// When
		when(jwtProperties.getAccessSecret()).thenReturn("test-secret");

		GatewayFilterChain chain = mock(GatewayFilterChain.class);

		// Then
		Mono<Void> result = filter.filter(exchange, chain).then(Mono.defer(() -> {
			assertThat(exchange.getResponse().getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.UNAUTHORIZED);
			return Mono.empty();
		}));
		result.block();
		verify(chain, times(0)).filter(exchange);

	}
}
