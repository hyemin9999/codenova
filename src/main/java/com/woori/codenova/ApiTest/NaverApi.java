package com.woori.codenova.ApiTest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Component(value = "naverApi")
public class NaverApi {
	@Value("${naver.client_id}")
	private String naverClientId;

	@Value("${naver.redirect_uri}")
	private String naverRedirectUri;

	@Value("${naver.client_secret}")
	private String naverClientSecret;

	public String getAccessToken(String code, String state) {
		String reqUrl = "https://nid.naver.com/oauth2.0/token";
		RestTemplate restTemplate = new RestTemplate();

		// HttpHeader Object
		HttpHeaders headers = new HttpHeaders();

		// HttpBody Object
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("grant_type", "authorization_code");
		params.add("client_id", naverClientId);
		params.add("client_secret", naverClientSecret);
		params.add("code", code);
		params.add("state", state);

		// http body params 와 http headers 를 가진 엔티티
		HttpEntity<MultiValueMap<String, String>> naverTokenRequest = new HttpEntity<>(params, headers);

		// reqUrl로 Http 요청, POST 방식
		ResponseEntity<String> response = restTemplate.exchange(reqUrl, HttpMethod.POST, naverTokenRequest,
				String.class);

		String responseBody = response.getBody();
		JsonObject asJsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
		return asJsonObject.get("access_token").getAsString();
	}

	public NaverProfile getUserInfo(String accessToken) {
		String reqUrl = "https://openapi.naver.com/v1/nid/me";

		RestTemplate restTemplate = new RestTemplate();

		// HttpHeader 오브젝트
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + accessToken);

		HttpEntity<MultiValueMap<String, String>> naverProfileRequest = new HttpEntity<>(headers);

		ResponseEntity<String> response = restTemplate.exchange(reqUrl, HttpMethod.GET, naverProfileRequest,
				String.class);
		System.out.println("네이버 API 응답: " + response.getBody());

		System.out.println("response = " + response);
		NaverProfile naverProfile = new NaverProfile(response.getBody());

		return naverProfile;
	}
}