package com.woori.codenova.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Category {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 게시판 분류 이름
	@Column(length = 200)
	private String name;

	// 등록날짜
	private LocalDateTime createDate;
	// 수정날짜
	private LocalDateTime modifyDate;

	// 추후에 게시글 == board 들어와야 함. @OneToMany?
}
