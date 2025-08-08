package com.woori.codenova.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter // Lombok: getter 메서드 자동 생성
@Setter // Lombok: setter 메서드 자동 생성
@Entity // JPA: 이 클래스가 DB 테이블과 매핑된다는 표시
public class Comment {

	@Id // 기본 키(PK)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	// auto_increment 전략: DB에서 자동 증가
	private Integer id;

	@Column(columnDefinition = "TEXT")
	// 긴 텍스트 저장을 위한 설정 (VARCHAR 대신 TEXT 사용)
	private String contents; // 댓글 내용

	private LocalDateTime createDate; // 생성일시

	private LocalDateTime modifyDate; // 수정일시

	private boolean isDelete; // 삭제 여부 (true면 삭제된 댓글)

	private LocalDateTime deleteDate; // 삭제된 시간

	@ManyToOne
	// 다대일 관계: 여러 개의 댓글(Comment)이 하나의 게시글(Board)에 속함
	private Board board; // 어떤 게시글에 달린 댓글인지 연결
}
