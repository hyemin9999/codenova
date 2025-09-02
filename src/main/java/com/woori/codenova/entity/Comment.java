package com.woori.codenova.entity;

import java.time.LocalDateTime;
import java.util.Set;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter // Lombok: getter 메서드 자동 생성
@Setter // Lombok: setter 메서드 자동 생성
@Entity // JPA: 이 클래스가 DB 테이블과 매핑된다는 표시
public class Comment {

	@Id // 기본 키(PK)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	// IDENTITY: DB의 auto_increment 를 사용하여 PK 값 생성
	private Integer id;

	// TEXT 컬럼: 긴 문자열(마크다운/코드 블록 등) 저장을 위해 사용, NOT NULL
	@Column(columnDefinition = "TEXT", nullable = false)
	private String contents; // 댓글 내용

	// 생성 시각(레코드 생성 시 반드시 세팅됨)
	@Column(nullable = false)
	private LocalDateTime createDate; // 생성(작성)일시

	// 수정 시각(수정 시에만 값이 존재할 수 있음)
	private LocalDateTime modifyDate; // 수정일시

	// 소프트 삭제 플래그(설계상 용도). 실제 소프트 삭제 로직은 서비스/레포지토리에서 처리 필요
	@Column(nullable = false)
	@ColumnDefault("false") // DB 기본값 false (DDL 자동 생성 시 반영될 수 있음)
	private boolean isDelete; // 삭제 여부 (true면 삭제된 댓글)

	// 소프트 삭제 시점(설계상 용도). 실제로 값 세팅은 비즈니스 로직에서 수행
	private LocalDateTime deleteDate; // 삭제된 시간

	@ManyToOne // 다:1 — 여러 댓글이 한 사용자(author)를 참조
	@JoinColumn(name = "userId") // FK: comment.user_id → site_user.id
	private SiteUser author; // 작성자

	// 연관관계의 주인: Comment.board (FK 소유). 반대편(Board.commentList)은 mappedBy로 컬렉션 보유
	@ManyToOne // 다:1 — 여러 댓글이 하나의 게시글(board)에 속함
	@JoinColumn(name = "boardId") // FK: comment.board_id → board.id
	private Board board; // 어떤 게시글에 달린 댓글인지 연결

	@ManyToMany // 다:다 — 댓글과 사용자 간 '추천' 관계
	@JoinTable(name = "commentVoter", joinColumns = @JoinColumn(name = "commentId"), inverseJoinColumns = @JoinColumn(name = "userId"))
	// Set 사용: 동일 사용자의 중복 추천 방지에 유리(중복 원소 허용하지 않음)
	Set<SiteUser> voter; // 이 댓글을 추천한 사용자 집합

	@ManyToMany // 다대다: 게시글-사용자 간 '추천' 관계
	@JoinTable(name = "commentFavorite", joinColumns = @JoinColumn(name = "commentId"), inverseJoinColumns = @JoinColumn(name = "userId"))
	Set<SiteUser> favorite; // 이 게시글을 추천한 사용자 집합(Set: 중복 추천 방지에 유리)

}
