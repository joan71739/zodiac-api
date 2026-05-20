package com.project.zodiac_api.dto;

import com.project.zodiac_api.model.ConsultationLog;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
public class ConsultationLogDto {

    private Integer id;

    // 諮詢日期為必填欄位，前端以 datetime-local 保護；後端同步驗證防止直接呼叫 API 繞過
    @NotNull(message = "諮詢日期不可為空")
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
