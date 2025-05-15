package com.nhnacademy.gateway.jwt.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RequestJwtTokenDTO {

	@NotNull
	private String memberId;
	@NotNull
	private String memberRole;

}