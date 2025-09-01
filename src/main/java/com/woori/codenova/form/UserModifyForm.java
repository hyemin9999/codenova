package com.woori.codenova.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserModifyForm {

	private Long id;
	private String username;

	@NotEmpty(message = "현재 비밀번호는 필수항목입니다.")
	private String password;

	@NotEmpty(message = "변경 비밀번호는 필수항목입니다.")
	private String password1;

	@NotEmpty(message = "변경 비밀번호 확인은 필수항목입니다.")
	private String password2;

	@Email
	private String email;

}
