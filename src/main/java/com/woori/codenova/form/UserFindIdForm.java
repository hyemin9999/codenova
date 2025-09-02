package com.woori.codenova.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserFindIdForm {

	@Email(message = "올바른 이메일 형식이 아닙니다.")
	@NotEmpty(message = "이메일은 필수항목입니다.")
	private String email;
}
