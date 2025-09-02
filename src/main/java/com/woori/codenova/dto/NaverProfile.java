package com.woori.codenova.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.Getter;

@Getter
public class NaverProfile {
	private String id;
	private String nickname;
	private String email;
	private String mobile;

	public NaverProfile(String jsonResponseBody) {
		JsonObject jsonObject = JsonParser.parseString(jsonResponseBody).getAsJsonObject();
		JsonObject response = jsonObject.getAsJsonObject("response");

		// 4. 선택 값(mobile)은 존재 여부를 확인하고 할당합니다. (안전성 강화)
		this.id = response.get("id").getAsString();
		this.nickname = response.get("nickname").getAsString();
		this.email = response.get("email").getAsString();
		if (response.has("mobile")) {
			this.mobile = response.get("mobile").getAsString();
		} else {
			this.mobile = null; // 정보가 없으면 null로 설정
		}

	}
}