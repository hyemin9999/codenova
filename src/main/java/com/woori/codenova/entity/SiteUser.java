package com.woori.codenova.entity;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class SiteUser {

	// pk선언 및 id 순번 부여
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 사용자ID
	@Column(unique = true, nullable = false)
	private String username;

	// 비밀번호
	@Column(nullable = false)
	private String password;

	// 이메일주소
	@Column(unique = true, nullable = false)
	private String email;

	// 가입일
	// 업데이트를 해도 수정이 안되며 db에서 수정해야함
	@Column(updatable = false)
	private LocalDateTime createDate;

	// 수정일
	// 마이페이지 구현되어야 사용가능
	private LocalDateTime modifyDate;

//	// 사용자 여부 구별
//	private boolean isUser;

	// 권한
	@ManyToMany
	Set<Role> authority;

//	String = varchar
// Integer = int
// Long = bigint

//	/**
//	 * 암호화된 새 비밀번호로 비밀번호를 업데이트하는 메서드
//	 * 
//	 * @param newEncodedPassword 새로 암호화된 비밀번호
//	 */
	public void updatePassword(String newEncodedPassword) {
		this.password = newEncodedPassword;
	}

	// 접속자는 어디인가 기본값 null. 나중에 local로 넣어줄예정
	private String provider;
	// 고유식별코드
	private String providerId;
}
