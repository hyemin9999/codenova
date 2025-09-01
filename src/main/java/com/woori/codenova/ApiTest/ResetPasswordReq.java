package com.woori.codenova.ApiTest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordReq {

	@NotBlank(message = "새로운 비밀번호를 입력해주세요.")
	@Size(min = 4, message = "비밀번호는 4자 이상이어야 합니다.")
	@Pattern(regexp = "^(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{4,}$", message = "비밀번호는 대문자 1개 이상, 특수문자 1개 이상을 포함해야 합니다.")
	private String newPassword;

	@NotBlank(message = "비밀번호 확인을 입력해주세요.")
	private String newPasswordConfirm;

}