package com.project.zodiac_api.dto;

import com.project.zodiac_api.model.Client;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
public class ClientResponseDto {

    private Integer id;
    private String name;
    private LocalDate birthDate;
    private LocalTime birthTime;
    private String birthPlace;

    // 上升 / 天頂四軸資訊（允許 null）
    private String  ascSign;
    private Short ascDegreeNum;
    private Short ascMinuteNum;

    private String  mcSign;
    private Short mcDegreeNum;
    private Short mcMinuteNum;

    private LocalDateTime createdAt;

    /** 單筆查詢用（含 ASC/MC）：GET /api/clients/{id} */
    public static ClientResponseDto from(Client c) {
        ClientResponseDto dto = new ClientResponseDto();
        dto.id           = c.getId();
        dto.name         = c.getName();
        dto.birthDate    = c.getBirthDate();
        dto.birthTime    = c.getBirthTime();
        dto.birthPlace   = c.getBirthPlace();
        dto.ascSign      = c.getAscSign();
        dto.ascDegreeNum = c.getAscDegreeNum();
        dto.ascMinuteNum = c.getAscMinuteNum();
        dto.mcSign       = c.getMcSign();
        dto.mcDegreeNum  = c.getMcDegreeNum();
        dto.mcMinuteNum  = c.getMcMinuteNum();
        dto.createdAt    = c.getCreatedAt();
        return dto;
    }
}
