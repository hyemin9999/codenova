package com.woori.codenova.service;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.woori.codenova.dto.AiDTO;

/**
 * AiService
 *
 * ▶ 역할
 * - Google Gemini API (Generative Language API) 호출을 담당하는 서비스 클래스
 * - 여러 개의 API Key를 순환(Round-Robin) 방식으로 사용하여 요청 처리
 * - 장애 발생 시(503 Service Unavailable) 자동으로 다른 키로 재시도
 *
 * ▶ 특징
 * - @Service: Spring 빈으로 등록
 * - API 호출 시 RestTemplate 사용 (동기 방식)
 *
 * ▶ 예외 처리
 * - 503(Service Unavailable): 다른 키로 재시도
 * - 그 외 에러: 그대로 예외 발생
 * - 모든 키가 실패할 경우 RuntimeException 발생
 */
@Service
public class AiService {
    
    /** API 키 배열 (여러 개일 경우 순환 사용) */
    private static final String[] API_KEYS = { 
        // TODO: 실제 API 키를 등록하거나, 환경변수/설정파일(application.yml)로 분리 권장
        "" 
    };

    /** API 키 인덱스를 Round-Robin 방식으로 관리하는 카운터 */
    private final AtomicInteger index = new AtomicInteger(0);

    /**
     * Gemini API 호출 메서드
     *
     * @param request GeminiRequest (프롬프트 및 입력 데이터 포함)
     * @return GeminiResponse (생성된 콘텐츠 결과)
     * @throws RuntimeException 모든 API 키가 과부하(503)로 실패한 경우
     */
    public AiDTO.GeminiResponse askGemini(AiDTO.GeminiRequest request) {
        int attempts = 0;
        int totalKeys = API_KEYS.length;

        while (attempts < totalKeys) {
            // 현재 API 키 선택 (Round-Robin 방식)
            int currentIndex = index.getAndUpdate(i -> (i + 1) % totalKeys);
            String apiKey = API_KEYS[currentIndex];

            // Gemini API URL (모델: gemini-1.5-flash)
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key="
                    + apiKey;

            try {
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                // 요청 데이터 (JSON)
                HttpEntity<AiDTO.GeminiRequest> entity = new HttpEntity<>(request, headers);

                // POST 요청 실행
                ResponseEntity<AiDTO.GeminiResponse> response =
                        restTemplate.postForEntity(url, entity, AiDTO.GeminiResponse.class);

                return response.getBody();

            } catch (HttpServerErrorException e) {
                // 503 (서비스 과부하) → 다른 키로 재시도
                if (e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                    attempts++;
                } else {
                    // 그 외 에러는 그대로 던짐
                    throw e;
                }
            }
        }

        // 모든 키 실패 시
        throw new RuntimeException("AI 과부하 상태입니다. 잠시 후 다시 시도해주세요.");
    }
}
