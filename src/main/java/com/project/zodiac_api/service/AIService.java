package com.project.zodiac_api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.zodiac_api.dto.AiChatRequestDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    @Value("${anthropic.api.key}")
    private String apiKey;

    @Value("${anthropic.api.model}")
    private String model;

    @Value("${anthropic.api.max-tokens}")
    private int maxTokens;

    private final ObjectMapper objectMapper;
    private final WebClient.Builder webClientBuilder;

    // 占星知識庫（啟動時載入，避免每次請求都讀檔）
    private String planetsKnowledge;
    private String housesKnowledge;
    private String signsKnowledge;
    private String aspectsKnowledge;

    @PostConstruct
    public void loadKnowledge() {
        planetsKnowledge = readResource("astrology/planets.txt");
        housesKnowledge  = readResource("astrology/houses.txt");
        signsKnowledge   = readResource("astrology/signs.txt");
        aspectsKnowledge = readResource("astrology/aspects.txt");
        log.info("[AI] 占星知識庫載入完成");
    }

    /**
     * 呼叫 Claude API，回傳 AI 回應文字
     */
    public String chat(AiChatRequestDto req) {
        String systemPrompt = buildSystemPrompt(req.getNoteTitle(), req.getNoteContent());

        // 組裝 messages：history + 本輪 user 訊息
        List<Map<String, String>> messages = new ArrayList<>();
        if (req.getHistory() != null) {
            messages.addAll(req.getHistory());
        }
        messages.add(Map.of("role", "user", "content", req.getUserMessage()));

        // 組裝 Claude API request body
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("max_tokens", maxTokens);
        body.put("system", systemPrompt);
        body.put("messages", messages);

        try {
            WebClient client = webClientBuilder
                    .baseUrl("https://api.anthropic.com")
                    .defaultHeader("x-api-key", apiKey)
                    .defaultHeader("anthropic-version", "2023-06-01")
                    .defaultHeader("content-type", MediaType.APPLICATION_JSON_VALUE)
                    .build();

            String responseBody = client.post()
                    .uri("/v1/messages")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // 解析回應：取 content[0].text
            Map<?, ?> responseMap = objectMapper.readValue(responseBody, Map.class);
            List<?> content = (List<?>) responseMap.get("content");
            if (content == null || content.isEmpty()) {
                throw new RuntimeException("Claude API 回傳空內容");
            }
            Map<?, ?> firstBlock = (Map<?, ?>) content.get(0);
            return (String) firstBlock.get("text");

        } catch (Exception e) {
            log.error("[AI] Claude API 呼叫失敗: {}", e.getMessage());
            throw new RuntimeException("AI 服務暫時無法使用，請稍後再試");
        }
    }

    // ── 私有方法 ─────────────────────────────────────────

    private String buildSystemPrompt(String noteTitle, String noteContent) {
        return """
                你是一位專業的占星顧問助理，協助占星師整理與分析命盤解讀筆記。

                ===== 占星知識庫 =====
                %s
                %s
                %s
                %s

                ===== 當前解析背景 =====
                標題：%s
                內容：%s

                請根據以上背景回應使用者的問題，回應以繁體中文為主。
                """.formatted(
                planetsKnowledge,
                housesKnowledge,
                signsKnowledge,
                aspectsKnowledge,
                noteTitle  != null ? noteTitle  : "（無標題）",
                noteContent != null ? noteContent : "（無內容）"
        );
    }

    private String readResource(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("[AI] 無法讀取知識庫檔案: {}，將使用空字串", path);
            return "";
        }
    }
}
