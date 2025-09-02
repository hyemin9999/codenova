package com.woori.codenova.service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RedisService {
	// 메일 인증 완료 서비스

	private final RedisTemplate<String, String> redisTemplate;

	@Transactional
	public void setValues(String key, String value) {
		redisTemplate.opsForValue().set(key, value);
	}

	@Transactional
	public void setValuesWitchTimeout(String key, String value, long timeout) {
		redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.MILLISECONDS);
	}

	public String getValues(String key) {
		return redisTemplate.opsForValue().get(key);
	}

	@Transactional
	public void deleteValues(String key) {
		redisTemplate.delete(key);
	}

}
