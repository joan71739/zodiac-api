package com.project.zodiac_api.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 解析參考面板 DTO
 *
 * 回傳結構：
 * {
 *   ascSection: {
 *     ascSign: "j",
 *     ascSignLabel: "天秤座",
 *     houses: [
 *       { houseNumber: 1, houseSign: "j", houseSignLabel: "天秤座", notes: [...] },
 *       ...
 *     ]
 *   },
 *   planetSections: [
 *     {
 *       planet: "Q", planetLabel: "太陽",
 *       sign: "g", signLabel: "獅子座", house: 10,
 *       signNotes: [...],
 *       signHouseNotes: [...],
 *       planetSignHouseNotes: [...]
 *     },
 *     ...
 *   ]
 * }
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReferencePanelDto {

    private AscSectionDto            ascSection;
    private List<PlanetSectionDto>   planetSections;

    // ── 上升區塊 ───────────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class AscSectionDto {
        private String          ascSign;
        private String          ascSignLabel;
        private List<HouseItemDto> houses;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class HouseItemDto {
        private Integer              houseNumber;
        private String               houseSign;
        private String               houseSignLabel;
        private List<NoteItemDto>    notes;         // sign_key=houseSign, planet_key=NULL, house_key=NULL
    }

    // ── 行星區塊 ───────────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class PlanetSectionDto {
        private String            planet;
        private String            planetLabel;
        private String            sign;
        private String            signLabel;
        private Integer           house;

        // 第一層：星座特性（planet_key=NULL, house_key=NULL）
        private List<NoteItemDto> signNotes;

        // 第二層：星座×宮位（planet_key=NULL, house_key=行星所在宮）
        private List<NoteItemDto> signHouseNotes;

        // 第三層：行星×星座×宮位（planet_key=行星, house_key=行星所在宮）
        private List<NoteItemDto> planetSignHouseNotes;
    }

    // ── 共用：筆記項目 ─────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class NoteItemDto {
        private Integer id;
        private String  title;
        private String  content;
        private String  topic;
    }
}
