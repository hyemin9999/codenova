package com.woori.codenova.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Getter // Lombok: getter 자동 생성 (모든 필드에 대한 getXxx() 메서드 생성)
@Setter // Lombok: setter 자동 생성 (모든 필드에 대한 setXxx(...) 메서드 생성)
@Entity // JPA: 이 클래스가 데이터베이스 테이블과 매핑됨(엔티티)
public class Board {

	@Id // 기본 키(PK) 컬럼 지정
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	// IDENTITY 전략: DB의 auto-increment(자증) 컬럼을 사용해 PK 생성
	private Integer id;

	@Column(nullable = false) // NOT NULL 제약
	private String subject; // 게시글 제목

	@Column(columnDefinition = "TEXT", nullable = false)
	// TEXT 타입: 긴 본문(마크다운 등)을 저장하기 위함, NOT NULL
	private String contents; // 게시글 내용

	@Column(nullable = false)
	@ColumnDefault("0")
	private int viewCount;

	@Column(nullable = false) // NOT NULL
	private LocalDateTime createDate; // 생성(작성) 일시

	private LocalDateTime modifyDate; // 수정 일시(수정 시에만 값 존재)

	@Column(nullable = false)
	@ColumnDefault("false") // DB 기본값 false (하이버네이트 DDL 자동 생성 시 반영될 수 있음)
	private boolean isDelete; // 소프트 삭제 여부(설계용 플래그) - 실제 소프트 삭제 로직은 별도 구현 필요

	private LocalDateTime deleteDate; // 삭제 일시(소프트 삭제 시각 기록용)

	@ManyToOne // 다대일: 여러 Board가 한 명의 SiteUser(author)를 참조
	@JoinColumn(name = "userId") // FK 컬럼명 지정 (board.user_id → site_user.id)
	private SiteUser author; // 작성자

	// 게시판(게시글 분류)
	@ManyToOne
	@JoinColumn(name = "categoryId", nullable = false)
	private Category category;

	@OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<Comment> commentList; // 이 게시글에 달린 모든 댓글(양방향: Comment.board)

	// 업로드 파일
	@OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE)
	private List<UploadFile> uploadFile;

	@ManyToMany // 다대다: 게시글-사용자 간 '추천' 관계
	@JoinTable(name = "boardVoter", joinColumns = @JoinColumn(name = "boardId"), inverseJoinColumns = @JoinColumn(name = "userId"))
	Set<SiteUser> voter; // 이 게시글을 추천한 사용자 집합(Set: 중복 추천 방지에 유리)

	@ManyToMany // 다대다: 게시글-사용자 간 '추천' 관계
	@JoinTable(name = "boardFavorite", joinColumns = @JoinColumn(name = "boardId"), inverseJoinColumns = @JoinColumn(name = "userId"))
	Set<SiteUser> favorite; // 이 게시글을 추천한 사용자 집합(Set: 중복 추천 방지에 유리)

}
