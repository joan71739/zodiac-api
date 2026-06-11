package com.project.zodiac_api.dto;

import com.project.zodiac_api.model.TransitNote;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
public class TransitNoteDto {

    private Integer id;

    private String transitPlanet;
    private String aspectType;
    private String natalPlanet;
    private Short  transitHouse;

    private String title;
    private String content;
    private String tag;

    // 主題分類：general / career / love / wealth / challenge；null = 未分類
    private String topic;

    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TransitNoteDto from(TransitNote n) {
        TransitNoteDto dto = new TransitNoteDto();
        dto.id            = n.getId();
        dto.transitPlanet = n.getTransitPlanet();
        dto.aspectType    = n.getAspectType();
        dto.natalPlanet   = n.getNatalPlanet();
        dto.transitHouse  = n.getTransitHouse();
        dto.title         = n.getTitle();
        dto.content       = n.getContent();
        dto.tag           = n.getTag();
        dto.topic         = n.getTopic();
        dto.sortOrder     = n.getSortOrder();
        dto.createdAt     = n.getCreatedAt();
        dto.updatedAt     = n.getUpdatedAt();
        return dto;
    }
}
