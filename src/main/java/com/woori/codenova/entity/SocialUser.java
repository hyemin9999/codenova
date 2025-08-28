package com.woori.codenova.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class SocialUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long UserId; // ✨ 새로운 PK

	private String nickname;
	private String email;
	private String provider; // kakao, local, naver, ....
	private String providerId; // 식별 코드

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "site_user_id") // SiteUser의 ID를 참조하는 FK
	private SiteUser siteUser;

	// Getters and Setters
}
