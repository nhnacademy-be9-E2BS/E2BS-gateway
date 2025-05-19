package com.nhnacademy.gateway.common.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.nhnacademy.gateway.jwt.properties.JwtProperties;
import com.nhnacademy.gateway.jwt.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class DevJwtAuthenticationFilter implements GlobalFilter, Ordered {

	private final JwtUtil jwtUtil;
	private final JwtProperties jwtProperties;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		return chain.filter(exchange);
	}

	@Override
	public int getOrder() {
		return -1;
	}

}
