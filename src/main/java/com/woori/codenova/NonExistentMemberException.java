package com.woori.codenova;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// HTTP 상태 코드 404(Not Found)를 반환하도록 설정
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NonExistentMemberException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	// 기본 생성자
	public NonExistentMemberException() {
		super("존재하지 않는 회원입니다.");
	}

	// 메시지를 인자로 받는 생성자
	public NonExistentMemberException(String message) {
		super(message);
	}
}