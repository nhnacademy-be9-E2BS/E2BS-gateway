package com.nhnacademy.gateway.filter;

import java.util.Objects;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.nhnacademy.gateway.jwt.rule.JwtRule;
import com.nhnacademy.gateway.jwt.status.TokenStatus;
import com.nhnacademy.gateway.jwt.properties.JwtProperties;
import com.nhnacademy.gateway.jwt.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

	private final JwtUtil jwtUtil;
	private final JwtProperties jwtProperties;

	/**
	 * 경로가 /api/auth 로 시작하면 chain.filter()
	 * 경로가 /api/** 로 시작하면 JWT Token 값이 AUTHENTICATED 인 회원만 경로 사용
	 */
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String path = exchange.getRequest().getURI().getPath();

		if(path.startsWith("/api/auth") || path.equals("/api/login") || path.equals("/api/register")) {
			return chain.filter(exchange);
		}

		String token = exchange.getRequest().getCookies()
			.getFirst(JwtRule.ACCESS_PREFIX.getValue()) != null ?
			Objects.requireNonNull(exchange.getRequest().getCookies().getFirst(JwtRule.ACCESS_PREFIX.getValue())).getValue() : "";

		String accessToken = jwtProperties.getAccessSecret();
		TokenStatus status = jwtUtil.getTokenStatus(token, jwtUtil.getSigningKey(accessToken));

		if(status != TokenStatus.AUTHENTICATED) {
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return exchange.getResponse().setComplete();
		}

		return chain.filter(exchange);
	}

	/**
	 * 우선 순위
	 */
	@Override
	public int getOrder() {
		return -1;
	}
}
