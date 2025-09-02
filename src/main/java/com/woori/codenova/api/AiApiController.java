package com.woori.codenova.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.woori.codenova.dto.AiDTO;
import com.woori.codenova.service.AiService;

@RestController
@RequestMapping("/api/ai")
public class AiApiController {
    private final AiService aiService;

    @Autowired
    public AiApiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/gemini")
    public AiDTO.GeminiResponse askGemini(@RequestBody AiDTO.GeminiRequest request) {
        return aiService.askGemini(request);
    }
}