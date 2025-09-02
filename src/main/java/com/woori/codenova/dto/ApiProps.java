package com.woori.codenova.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component // 이 클래스를 Spring Bean으로 등록합니다.
@Getter
@Setter
@ConfigurationProperties(prefix = "props") // "props."로 시작하는 속성들을 바인딩합니다.
public class ApiProps {
	private String resetPasswordUrl;
	// 다른 속성들이 있다면 여기에 추가하면 됩니다.
}