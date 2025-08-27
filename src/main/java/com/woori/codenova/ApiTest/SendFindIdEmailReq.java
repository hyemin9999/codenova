package com.woori.codenova.ApiTest;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SendFindIdEmailReq {

	@Email(message = "유효한 이메일 주소를 입력해주세요.")
	@NotBlank(message = "이메일은 필수 입력 항목입니다.")
	private String email;
}