package com.woori.codenova.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

public class AiDTO {

    // ===============================================================
    // 📌 Gemini API 요청(Request) DTO
    // ===============================================================
    @Data // Lombok: getter/setter, toString, equals 등 자동 생성
    @NoArgsConstructor // 기본 생성자 자동 생성
    public static class GeminiRequest {
        public List<Content> contents; 
        // 요청에 포함될 "내용" (role + parts 구조로 이루어짐)

        @Data
        @NoArgsConstructor
        public static class Content {
            public String role; // 역할 (예: "user" = 사용자 질문, "model" = AI 응답)
            public List<Part> parts; // 메시지를 여러 조각(Part)으로 나눠 담음

            @Data
            @NoArgsConstructor
            public static class Part {
                public String text; // 실제 텍스트 입력 (질문/프롬프트 내용)
            }
        }
    }

    // ===============================================================
    // 📌 Gemini API 응답(Response) DTO
    // ===============================================================
    @Data
    @NoArgsConstructor
    public static class GeminiResponse {
        public List<Candidate> candidates; 
        // Gemini 모델이 반환한 응답 후보들 (여러 개일 수 있음)

        @Data
        @NoArgsConstructor
        public static class Candidate {
            public Content content; // 후보 응답의 실제 내용

            @Data
            @NoArgsConstructor
            public static class Content {
                public List<Part> parts; // 응답 텍스트가 조각(Part) 형태로 전달됨

                @Data
                @NoArgsConstructor
                public static class Part {
                    public String text; // 실제 AI가 생성한 응답 텍스트
                }
            }
        }
    }
}
