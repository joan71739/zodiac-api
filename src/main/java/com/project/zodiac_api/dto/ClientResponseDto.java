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
    private LocalDateTime createdAt;

    public static ClientResponseDto from(Client c) {
        ClientResponseDto dto = new ClientResponseDto();
        dto.id          = c.getId();
        dto.name        = c.getName();
        dto.birthDate   = c.getBirthDate();
        dto.birthTime   = c.getBirthTime();
        dto.birthPlace  = c.getBirthPlace();
        dto.createdAt   = c.getCreatedAt();
        return dto;
    }
}
