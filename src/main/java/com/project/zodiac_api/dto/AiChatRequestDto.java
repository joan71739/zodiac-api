package com.project.zodiac_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor
public class AiChatRequestDto {

    private String noteTitle;
    private String noteContent;
    private List<Map<String, String>> history;  // [{ "role": "user"|"assistant", "content": "..." }]

    // Fix #5：統一用 @NotBlank 做 DTO 層驗證，搭配 Controller 的 @Valid
    @NotBlank(message = "userMessage 不得為空")
    private String userMessage;
}