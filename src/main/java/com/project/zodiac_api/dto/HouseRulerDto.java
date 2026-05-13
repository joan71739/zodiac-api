package com.project.zodiac_api.dto;

import com.project.zodiac_api.model.HouseRuler;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter @Setter
@NoArgsConstructor
public class HouseRulerDto {

    private Integer id;
    private Integer houseNumber;
    private String rulingPlanet;
    private String fliesToSign;
    private Integer fliesToHouse;

    public static HouseRulerDto from(HouseRuler h) {
        HouseRulerDto dto = new HouseRulerDto();
        dto.id           = h.getId();
        dto.houseNumber  = h.getHouseNumber();
        dto.rulingPlanet = h.getRulingPlanet();
        dto.fliesToSign  = h.getFliesToSign();
        dto.fliesToHouse = h.getFliesToHouse();
        return dto;
    }
}
