package com.woori.codenova;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// HTTP 상태 코드 400(Bad Request)을 반환하도록 설정
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidUuidException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InvalidUuidException() {
		super("유효하지 않은 UUID입니다.");
	}
}