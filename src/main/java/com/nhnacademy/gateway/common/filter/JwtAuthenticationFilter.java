package com.nhnacademy.gateway.common.filter;

import java.util.Objects;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.nhnacademy.gateway.jwt.rule.JwtRule;
import com.nhnacademy.gateway.jwt.status.TokenStatus;
import com.nhnacademy.gateway.jwt.properties.JwtProperties;
import com.nhnacademy.gateway.jwt.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Profile("!dev")
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

	private final JwtUtil jwtUtil;
	private final JwtProperties jwtProperties;

	/**
	 * 경로가 /api/auth 로 시작하면 chain.filter()
	 * 경로가 /api/** 로 시작하면 JWT Token 값이 AUTHENTICATED 인 회원만 경로 사용
	 *
	 * front - gateway - auth
	 * front - gateway - back
	 *
	 * 원래는 auth 에서 쿠키를 만들어서 쿠키 안에 JWT 토큰이 있는지 확인하면서 인증을 해야 하지만
	 * 쿠키의 값은 (response) 서버마다 있기 때문에 front , auth 의 쿠키 값이 달라 공유할 수 없다
	 * 따라서 쿠키는 Front 에만 만들어주고 front 에서 api 요청을 할 때마다 인터셉터를 통해 요청에 쿠키 값을 헤더에 넣어 보낸다
 	 * 헤더에 보낸 쿠키 값을 gateway 필터에서 확인하고 있으면 유효한지 확인하여 경로로 이동한다
	 *
	 */
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String path = exchange.getRequest().getURI().getPath();

		if(path.startsWith("/api/auth") || path.equals("/api/login") || path.equals("/api/register") || path.equals("/api/categories") || path.equals("/api/categories/*")) {
			return chain.filter(exchange);
		}

		String header = exchange.getRequest().getHeaders().getFirst(JwtRule.JWT_ISSUE_HEADER.getValue());
		String token = "";
		if(Objects.nonNull(header) && header.contains("=")) {
			token = header.substring(header.indexOf("=") + 1).trim();
		}

		String secretKey = jwtProperties.getAccessSecret();
		TokenStatus status = jwtUtil.getTokenStatus(token, jwtUtil.getSigningKey(secretKey));

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
