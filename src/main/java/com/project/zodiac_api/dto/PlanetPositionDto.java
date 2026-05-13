package com.project.zodiac_api.dto;

import com.project.zodiac_api.model.PlanetPosition;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter @Setter
@NoArgsConstructor
public class PlanetPositionDto {

    private Integer id;       // 新增時為 null，回傳時填入
    private String planet;
    private String sign;
    private Short degreeNum;
    private Short minuteNum;
    private Integer house;
    private String notes;

    public static PlanetPositionDto from(PlanetPosition p) {
        PlanetPositionDto dto = new PlanetPositionDto();
        dto.id        = p.getId();
        dto.planet    = p.getPlanet();
        dto.sign      = p.getSign();
        dto.degreeNum = p.getDegreeNum();
        dto.minuteNum = p.getMinuteNum();
        dto.house     = p.getHouse();
        dto.notes     = p.getNotes();
        return dto;
    }
}
