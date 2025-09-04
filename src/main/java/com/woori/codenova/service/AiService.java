package com.woori.codenova.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.woori.codenova.dto.AiDTO;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AiService {

    // API 키 나눠쓰기
    private static final String[] API_KEYS = {


    };

    private final AtomicInteger index = new AtomicInteger(0);

    // 요청보내기
    public AiDTO.GeminiResponse askGemini(AiDTO.GeminiRequest request) {
        int attempts = 0;
        int totalKeys = API_KEYS.length;

        while (attempts < totalKeys) {
            int currentIndex = index.getAndUpdate(i -> (i + 1) % totalKeys);
            String apiKey = API_KEYS[currentIndex];
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

            try {
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<AiDTO.GeminiRequest> entity = new HttpEntity<>(request, headers);

                ResponseEntity<AiDTO.GeminiResponse> response = restTemplate.postForEntity(url, entity, AiDTO.GeminiResponse.class);
                return response.getBody();
            } catch (HttpServerErrorException e) {
                if (e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                    attempts++;
                } else {
                    throw e;
                }
            }
        }

        throw new RuntimeException("AI 과부하 상태입니다. 잠시 후 다시 시도해주세요.");
    }
}
