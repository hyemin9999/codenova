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
public class SendEmail {
//	고유번호/이메일/식별코드/발송시간/요구횟수/제제여부/제제만료시각 --> 이건 table말고 세션 처리할거

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long UserId; // 고유 PK 번호

	@Column(unique = true, nullable = false)
	private String email; // 판별 수단 Email 무조건 필수적으로 들어가야함

	@Column(unique = true, nullable = false)
	private String sendUuid; // 발송된 링크나 문자코드를 확인가능 [ 시스템 에러일 경우 문의시 DB에서 볼수 있음 ]

	private LocalDateTime sendTime; // 발송시작시 LocalDate.now()로 저장해버림

	@Column(nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int requestCount; // 요청횟수 4회이상 찍히는 순간 이메일 접근이 거부되며 제약에 걸림

	private boolean isSanctioned; // 제제여부 true = 제제중 | false = 제제아님

	private LocalDateTime isSanctionedtime; // 제제 만료시간을 표시해줌 | 기본 마지막 요청으로부터 30분뒤

}
