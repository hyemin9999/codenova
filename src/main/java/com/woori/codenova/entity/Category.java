package com.woori.codenova.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Category {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	// 게시판 분류 이름
	@Column(length = 50, nullable = false)
	private String name;

	// 등록날짜
	@Column(nullable = false)
	private LocalDateTime createDate;
	// 수정날짜
	private LocalDateTime modifyDate;

	// 게시글 == board
	@OneToMany(mappedBy = "category", cascade = CascadeType.REMOVE)
	private List<Board> boardList;

	// 공지사항 - 게시글
	@OneToMany(mappedBy = "category", cascade = CascadeType.REMOVE)
	private List<Notice> noticeList;

	@OneToMany(mappedBy = "category", cascade = CascadeType.REMOVE)
	private List<SearchText> searchtextList;

	// 사용여부?? (생성한 카테고리의 사용여부를 설정할수 있어야 할까?)

	// 즐겨찾기
	@ManyToMany
	@JoinTable(name = "categoryFavorite", joinColumns = @JoinColumn(name = "categoryId"), inverseJoinColumns = @JoinColumn(name = "userId"))
	Set<SiteUser> favorite;
}
