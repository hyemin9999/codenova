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
public class Role {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	// 권한 이름
	@Column(nullable = false)
	private String name;

	// 등급 (등급을 기본으로 0(슈퍼관리자),1(일반 사용자) 으로 만들어 두는게 나을까? nullable = false 하고싶은데)
//	@Column(nullable = false)
//	@ColumnDefault()
	private Integer grade;

	// 등록날짜
	@Column(nullable = false)
	private LocalDateTime createDate;
	// 수정날짜
	private LocalDateTime modifyDate;

}