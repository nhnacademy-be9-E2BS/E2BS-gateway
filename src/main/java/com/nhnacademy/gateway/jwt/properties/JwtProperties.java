package com.nhnacademy.gateway.jwt.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
	private String accessSecret;
	private String refreshSecret;
	private long accessExpiration;
	private long refreshExpiration;
}
