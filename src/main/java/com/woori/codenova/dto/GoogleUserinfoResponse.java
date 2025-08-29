package com.woori.codenova.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자를 만들어줍니다.
public class GoogleUserinfoResponse {
	private String email;
	private String sub; // 사용자의 고유 식별자
	private String name; // 사용자 이름 (닉네임)
}
