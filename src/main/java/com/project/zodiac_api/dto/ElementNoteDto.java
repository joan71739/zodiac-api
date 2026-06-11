package com.project.zodiac_api.dto;

import com.project.zodiac_api.model.ElementNote;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
public class ElementNoteDto {

    private Integer id;

    private String signKey;
    private String planetKey;
    private Short  houseKey;

    private String title;
    private String content;
    private String tag;

    // 主題分類：general / career / love / wealth / challenge；null = 未分類
    private String topic;

    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ElementNoteDto from(ElementNote n) {
        ElementNoteDto dto = new ElementNoteDto();
        dto.id         = n.getId();
        dto.signKey    = n.getSignKey();
        dto.planetKey  = n.getPlanetKey();
        dto.houseKey   = n.getHouseKey();
        dto.title      = n.getTitle();
        dto.content    = n.getContent();
        dto.tag        = n.getTag();
        dto.topic      = n.getTopic();
        dto.sortOrder  = n.getSortOrder();
        dto.createdAt  = n.getCreatedAt();
        dto.updatedAt  = n.getUpdatedAt();
        return dto;
    }
}
