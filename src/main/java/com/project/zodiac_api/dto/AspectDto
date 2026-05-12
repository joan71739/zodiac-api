package com.project.zodiac_api.dto;

import com.project.zodiac_api.model.Aspect;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
public class AspectDto {

    private Integer id;
    private String planet1;
    private String aspectType;
    private String planet2;
    private BigDecimal orb;
    private String notes;

    public static AspectDto from(Aspect a) {
        AspectDto dto = new AspectDto();
        dto.id         = a.getId();
        dto.planet1    = a.getPlanet1();
        dto.aspectType = a.getAspectType();
        dto.planet2    = a.getPlanet2();
        dto.orb        = a.getOrb();
        dto.notes      = a.getNotes();
        return dto;
    }
}
