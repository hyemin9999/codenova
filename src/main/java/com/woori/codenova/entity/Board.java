package com.woori.codenova.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Getter // Lombok: getter 자동 생성
@Setter // Lombok: setter 자동 생성
@Entity // JPA: 이 클래스는 데이터베이스 테이블과 매핑됨
public class Board {

	@Id // 기본 키(PK)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	// auto_increment 전략: DB가 자동으로 숫자를 증가시켜 ID 부여
	private Integer id;

	private String subject; // 게시글 제목

	@Column(columnDefinition = "TEXT")
	// 기본 문자열 타입보다 더 긴 내용을 허용하는 TEXT 컬럼으로 지정
	private String contents; // 게시글 내용

	private int viewCount; // 조회수

	private LocalDateTime createDate; // 생성 일시

	private LocalDateTime modifyDate; // 수정 일시

	private boolean isDelete; // 삭제 여부 (true: 삭제됨, false: 정상)

	private LocalDateTime deleteDate; // 삭제된 시각 (삭제된 경우에만 값 있음)

	@OneToMany(mappedBy = "board")
	// 하나의 게시글(Board)이 여러 개의 댓글(Comment)을 가짐
	// mappedBy는 Comment 엔티티의 "board" 필드를 참조
	private List<Comment> commentList; // 이 게시글에 달린 모든 댓글 목록
}
