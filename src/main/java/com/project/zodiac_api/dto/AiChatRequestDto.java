package com.project.zodiac_api.dto;

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
    private String userMessage;
}
