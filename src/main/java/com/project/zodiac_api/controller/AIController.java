package com.project.zodiac_api.controller;

import com.project.zodiac_api.dto.AiChatRequestDto;
import com.project.zodiac_api.service.AIService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    // POST /api/ai/chat
    // Fix #5：改用 @Valid 統一驗證，由 GlobalExceptionHandler 處理 400 回應
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@Valid @RequestBody AiChatRequestDto req) {
        String reply = aiService.chat(req);
        return ResponseEntity.ok(Map.of("reply", reply));
    }
}