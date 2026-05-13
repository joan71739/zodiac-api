package com.project.zodiac_api.dto;

import com.project.zodiac_api.model.ConsultationLog;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
public class ConsultationLogDto {

    private Integer id;
    private LocalDateTime consultationDate;
    private String notes;
    private LocalDateTime createdAt;

    public static ConsultationLogDto from(ConsultationLog l) {
        ConsultationLogDto dto = new ConsultationLogDto();
        dto.id               = l.getId();
        dto.consultationDate = l.getConsultationDate();
        dto.notes            = l.getNotes();
        dto.createdAt        = l.getCreatedAt();
        return dto;
    }
}
