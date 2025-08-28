package com.woori.codenova.ApiTest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin("*") // 뭔지 모름
public class GoogleController {

	@Value("${google.client.id}")
	private String googleClientId;
	@Value("${google.client.pw}")
	private String googleClientPw;

	@RequestMapping(value = "/api/v1/oauth2/google", method = RequestMethod.POST)
	public String loginUrlGoogle() {
		String reqUrl = "https://accounts.google.com/o/oauth2/v2/auth?client_id=" + googleClientId
				+ "&redirect_uri=http://localhost:8080/api/v1/oauth2/google&response_type=code&scope=email%20profile%20openid&access_type=offline";
		return reqUrl;
	}
}
