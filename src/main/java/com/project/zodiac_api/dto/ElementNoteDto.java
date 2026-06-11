package com.project.zodiac_api.dto;

import com.project.zodiac_api.model.ElementNote;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
public class ElementNoteDto {

    private Integer id;

    // 查詢用（GET 時回傳供前端確認，POST/PUT 時由路徑參數傳入，不從 body 取）
    private String signKey;
    private String planetKey;   // null = 純星座解析
    private Short  houseKey;    // null = 星座特性頁籤

    private String title;
    private String content;
    private String tag;         // 第一批先開欄位，第二批細作 UI

    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ElementNoteDto from(ElementNote n) {
        ElementNoteDto dto = new ElementNoteDto();
        dto.id         = n.getId();
        dto.signKey    = n.getSignKey();
        dto.planetKey  = n.getPlanetKey();
        dto.houseKey   = n.getHouseKey();
        dto.title      = n.getTitle();
        dto.content    = n.getContent();
        dto.tag        = n.getTag();
        dto.sortOrder  = n.getSortOrder();
        dto.createdAt  = n.getCreatedAt();
        dto.updatedAt  = n.getUpdatedAt();
        return dto;
    }
}
