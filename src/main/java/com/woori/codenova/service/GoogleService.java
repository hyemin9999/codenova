package com.woori.codenova.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.woori.codenova.dto.GoogleInfResponse;
import com.woori.codenova.dto.GoogleRequest;
import com.woori.codenova.dto.GoogleResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class GoogleService {

	@Value("${google.client_id}")
	private String googleClientId;
	@Value("${google.client_pw}")
	private String googleClientPw;
	@Value("${social.secret-key}")
	private String socialSecretKey;
	@Value("${google.redirect_uri}")
	private String googleRedirect;

//	@RequestMapping(value = "/api/v1/oauth2/google", method = RequestMethod.POST)
	public String loginUrlGoogle() {
		String reqUrl = "https://accounts.google.com/o/oauth2/v2/auth?client_id=" + googleClientId + "&redirect_uri="
				+ googleRedirect + "&response_type=code&scope=email%20profile%20openid&access_type=offline";
		return reqUrl;
	}

	public GoogleInfResponse UserInfo(String authCode) {
		RestTemplate restTemplate = new RestTemplate();
		GoogleRequest googleOAuthRequestParam = GoogleRequest.builder().clientId(googleClientId)
				.clientSecret(googleClientPw).code(authCode).redirectUri(googleRedirect).grantType("authorization_code")
				.build();
		ResponseEntity<GoogleResponse> resultEntity = restTemplate.postForEntity("https://oauth2.googleapis.com/token",
				googleOAuthRequestParam, GoogleResponse.class);
		String jwtToken = resultEntity.getBody().getId_token();
		Map<String, String> map = new HashMap<>();
		map.put("id_token", jwtToken);
		ResponseEntity<GoogleInfResponse> resultEntity2 = restTemplate
				.postForEntity("https://oauth2.googleapis.com/tokeninfo", map, GoogleInfResponse.class);
		GoogleInfResponse userInfo = resultEntity2.getBody();
		return userInfo;
	}
}
