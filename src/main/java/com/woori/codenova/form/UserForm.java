package com.woori.codenova.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserForm {

	@Size(min = 3, max = 25, message = "아이디는 3글자 이상 25글자 이하만 가능합니다.")
	@NotEmpty(message = "사용자ID는 필수항목입니다.")
	private String username;

	@NotEmpty(message = "비밀번호는 필수항목입니다.")
	@Size(min = 4, message = "비밀번호는 4자 이상이어야 합니다.")
	@Pattern(regexp = "^(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{4,}$", message = "비밀번호는 4자 이상, 대문자 1개 이상, 특수문자 1개 이상을 포함해야 합니다.")
	private String password1;

	@NotEmpty(message = "비밀번호확인은 필수항목입니다.")
	private String password2;

	@NotEmpty(message = "이메일은 필수항목입니다.")
	// Email 검증이 @만 들어가도 인식되어서 나중에 다시 수정요망
	// 테스트중
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	private String email;

}
