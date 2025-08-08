package com.woori.codenova.form;

import jakarta.validation.constraints.NotEmpty; // 빈 문자열 또는 null을 허용하지 않도록 검증
import lombok.Getter; // Lombok: Getter 메서드 자동 생성
import lombok.Setter; // Lombok: Setter 메서드 자동 생성

@Getter
@Setter
public class CommentForm {

	// 댓글 내용 필드
	@NotEmpty(message = "내용은 필수항목입니다.") // 값이 비어 있으면 검증 실패 (유효성 검사)
	private String contents;
}
