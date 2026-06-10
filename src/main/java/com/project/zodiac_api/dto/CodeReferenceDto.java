package com.project.zodiac_api.dto;

import com.project.zodiac_api.model.CodeReference;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter @Setter
@NoArgsConstructor
public class CodeReferenceDto {

    private Integer id;
    private String code;
    private String category;
    private String zhName;

    public static CodeReferenceDto from(CodeReference c) {
        CodeReferenceDto dto = new CodeReferenceDto();
        dto.id       = c.getId();
        dto.code     = c.getCode();
        dto.category = c.getCategory();
        dto.zhName   = c.getZhName();
        return dto;
    }
}
