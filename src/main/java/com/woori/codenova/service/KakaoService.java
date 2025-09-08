package com.woori.codenova.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.woori.codenova.dto.KakaoTokenResponseDto;
import com.woori.codenova.dto.KakaoUserInfoResponseDto;

import io.netty.handler.codec.http.HttpHeaderValues;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class KakaoService {

	@Value("${kakao.redirect_uri}")
	private String redirect_uri;
	private String clientId;
	private final String KAUTH_TOKEN_URL_HOST;
	private final String KAUTH_USER_URL_HOST;

	@Autowired
	public KakaoService(@Value("${kakao.client_id}") String clientId) {
		this.clientId = clientId;
		KAUTH_TOKEN_URL_HOST = "https://kauth.kakao.com";
		KAUTH_USER_URL_HOST = "https://kapi.kakao.com";
	}

	public KakaoUserInfoResponseDto getaccessToken(String authCode) {

//	KakaoUserInfoResponseDto userInfo = getUserInfo(accessToken);
		String accessToken = getAccessTokenFromKakao(authCode);
		return getUserInfo(accessToken);
	}

	public String getAccessTokenFromKakao(String code) {

//		String redirectUri = "${kakao"; 잠궈버리고 yml에 있는 kakao 경로로 대체함

		KakaoTokenResponseDto kakaoTokenResponseDto = WebClient.create(KAUTH_TOKEN_URL_HOST).post()
				.uri(uriBuilder -> uriBuilder.scheme("https").path("/oauth/token")
						.queryParam("grant_type", "authorization_code").queryParam("client_id", clientId)
						.queryParam("redirect_uri", redirect_uri).queryParam("code", code).build(true))
				.header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
				.retrieve()
				// TODO : Custom Exception
				.onStatus(HttpStatusCode::is4xxClientError,
						clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
				.onStatus(HttpStatusCode::is5xxServerError,
						clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
				.bodyToMono(KakaoTokenResponseDto.class).block();

		// 제공 조건: OpenID Connect가 활성화 된 앱의 토큰 발급 요청인 경우 또는 scope에 openid를 포함한 추가 항목 동의
		// 받기 요청을 거친 토큰 발급 요청인 경우

		return kakaoTokenResponseDto.getAccessToken();
	}

	public KakaoUserInfoResponseDto getUserInfo(String accessToken) {

		KakaoUserInfoResponseDto userInfo = WebClient.create(KAUTH_USER_URL_HOST).get()
				.uri(uriBuilder -> uriBuilder.scheme("https").path("/v2/user/me").build(true))
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken) // access token 인가
				.header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
				.retrieve()
				// TODO : Custom Exception
				.onStatus(HttpStatusCode::is4xxClientError,
						clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
				.onStatus(HttpStatusCode::is5xxServerError,
						clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
				.bodyToMono(KakaoUserInfoResponseDto.class).block();

		return userInfo;
	}
}
