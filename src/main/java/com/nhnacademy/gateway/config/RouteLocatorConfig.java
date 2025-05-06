package com.nhnacademy.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteLocatorConfig {

	/*
	 * 예시)
	 * account-service : 해당 route의 id로 식별 용도
	 * p->p.path("/account") : /account 경로에 대한 매핑
	 * .and() 이 이외의 추가 조건을 붙일 때
	 * .uri("lb://ACCOUNT-SERVICE") ACCOUNT-SERVICE 라는 이름의 API에 로드 밸런싱으로 요청을 보낸다.(유레카에 등록된 이름을 써야 한다.)
	 * */

	@Bean
	public RouteLocator myRoute(RouteLocatorBuilder builder) {

		RouteLocator routeLocator = builder.routes().build();

		return builder.routes()
			.route("account-service",
				p -> p.path("/account").and()
					.uri("lb://ACCOUNT-SERVICE")
			)
			// 메인 화면 띄우기
			.route("front",
				p -> p.path("/index").and()
					.uri("lb://FRONT"))
			.build();
	}
}
