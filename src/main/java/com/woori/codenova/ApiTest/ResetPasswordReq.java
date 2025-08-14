package com.woori.codenova.ApiTest;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordReq {

	@NotBlank(message = "새로운 비밀번호를 입력해주세요.")
	private String newPassword;

	@NotBlank(message = "비밀번호 확인을 입력해주세요.")
	private String newPasswordConfirm;

}