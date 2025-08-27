package com.woori.codenova.form;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter // Lombok: getter 메서드 자동 생성
@Setter // Lombok: setter 메서드 자동 생성
public class BoardForm {

	// 게시글 제목 필드
	@NotEmpty(message = "제목은 필수항목입니다.") // 제목이 비어 있으면 검증 실패
	@Size(max = 200) // 제목의 최대 길이는 200자
	private String subject;

	// 게시글 내용 필드
	@NotEmpty(message = "내용은 필수항목입니다.") // 내용이 비어 있으면 검증 실패
	private String contents;
}