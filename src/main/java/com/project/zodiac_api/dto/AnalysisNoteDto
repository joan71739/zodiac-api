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
    private String title;
    private String content;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AnalysisNoteDto from(AnalysisNote n) {
        AnalysisNoteDto dto = new AnalysisNoteDto();
        dto.id        = n.getId();
        dto.title     = n.getTitle();
        dto.content   = n.getContent();
        dto.sortOrder = n.getSortOrder();
        dto.createdAt = n.getCreatedAt();
        dto.updatedAt = n.getUpdatedAt();
        return dto;
    }
}
