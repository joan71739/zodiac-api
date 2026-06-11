package com.project.zodiac_api.dto;

import com.project.zodiac_api.model.AnalysisNote;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
public class AnalysisNoteDto {

    private Integer id;
    private String  title;
    private String  content;

    // ── v17 新增：選填標記欄位 ──────────────────────────
    private String planetKey;   // 對應行星（選填）
    private String signKey;     // 對應星座（選填）
    private Short  houseKey;    // 對應宮位（選填）
    private String topic;       // 主題分類（選填）
    // ────────────────────────────────────────────────────

    private Integer       sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AnalysisNoteDto from(AnalysisNote n) {
        AnalysisNoteDto dto = new AnalysisNoteDto();
        dto.id         = n.getId();
        dto.title      = n.getTitle();
        dto.content    = n.getContent();
        dto.planetKey  = n.getPlanetKey();
        dto.signKey    = n.getSignKey();
        dto.houseKey   = n.getHouseKey();
        dto.topic      = n.getTopic();
        dto.sortOrder  = n.getSortOrder();
        dto.createdAt  = n.getCreatedAt();
        dto.updatedAt  = n.getUpdatedAt();
        return dto;
    }
}
