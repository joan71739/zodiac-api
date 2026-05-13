package com.project.zodiac_api.controller;

import com.project.zodiac_api.dto.AiChatRequestDto;
import com.project.zodiac_api.service.AIService;
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
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody AiChatRequestDto req) {
        if (req.getUserMessage() == null || req.getUserMessage().isBlank()) {
            throw new IllegalArgumentException("userMessage 不得為空");
        }
        String reply = aiService.chat(req);
        return ResponseEntity.ok(Map.of("reply", reply));
    }
}
