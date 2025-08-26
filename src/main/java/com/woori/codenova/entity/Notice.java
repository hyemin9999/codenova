package com.woori.codenova.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Notice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	// 제목
	@Column(nullable = false)
	private String subject;

	// 내용
	@Column(columnDefinition = "TEXT", nullable = false)
	private String contents;

	// 조회수
	@Column(nullable = false)
	@ColumnDefault("0")
	private Integer viewCount;

	// 작성날짜
	@Column(nullable = false)
	private LocalDateTime createDate;
	// 수정날짜
	private LocalDateTime modifyDate;

	// 작성자
	@ManyToOne
	@JoinColumn(name = "user_id")
	private SiteUser author;

	// 게시판(게시글 분류)
	@ManyToOne
	@JoinColumn(name = "category_id")
	private Category category;

	// 업로드 파일
	@OneToMany(mappedBy = "notice", cascade = CascadeType.REMOVE)
	private List<UploadFile> uploadFile;

}