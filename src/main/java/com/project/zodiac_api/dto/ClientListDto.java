package com.project.zodiac_api.dto;

import com.project.zodiac_api.model.Client;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * 客戶列表 DTO（不含 ASC/MC）。
 * 供 GET /api/clients 使用，避免列表端點回傳不必要的四軸欄位。
 */
@Getter @Setter
@NoArgsConstructor
public class ClientListDto {

    private Integer id;
    private String name;
    private LocalDate birthDate;
    private LocalTime birthTime;
    private String birthPlace;
    private LocalDateTime createdAt;

    public static ClientListDto from(Client c) {
        ClientListDto dto = new ClientListDto();
        dto.id        = c.getId();
        dto.name      = c.getName();
        dto.birthDate = c.getBirthDate();
        dto.birthTime = c.getBirthTime();
        dto.birthPlace = c.getBirthPlace();
        dto.createdAt = c.getCreatedAt();
        return dto;
    }
}
