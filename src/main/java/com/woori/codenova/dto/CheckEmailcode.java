package com.woori.codenova.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckEmailcode {

	@Email
	private String email;

	private String uuid;
}
