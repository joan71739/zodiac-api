package com.project.zodiac_api.dto;

import com.project.zodiac_api.model.ChartPreferences;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * GET /api/chart/preferences 回傳 / PUT Request Body 共用 DTO。
 *
 * 結構說明（巢狀，非扁平）：
 * {
 *   "aspects": { "conjunction": { "orb": 8.0, "show": true }, ... },
 *   "strictMode": false,
 *   "planetOrbs": { "sun": 8.0, ... },
 *   "planetVisibility": { "uranus": true, ..., "chiron": false, ... }
 * }
 *
 * DB 欄位（snake_case）← Jackson 無法自動處理巢狀結構 → 需手寫 from() mapping。
 * 使用 LinkedHashMap 保持 JSON key 順序，方便前後端對照。
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ChartPreferencesDTO {

    private Map<String, AspectPref> aspects;
    private Boolean strictMode;
    private Map<String, Double>  planetOrbs;
    private Map<String, Boolean> planetVisibility;

    // ── 相位設定內層結構 ────────────────────────────────────────────────
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class AspectPref {
        private Double  orb;
        private Boolean show;
    }

    // ── Entity → DTO ───────────────────────────────────────────────────
    /**
     * 把扁平的 ChartPreferences Entity（48 欄）組裝成巢狀 DTO。
     * 各 Map 使用 LinkedHashMap，確保 JSON 序列化順序可預期。
     */
    public static ChartPreferencesDTO from(ChartPreferences e) {
        ChartPreferencesDTO dto = new ChartPreferencesDTO();

        // aspects（10 個相位，camelCase key 與前端 ASPECT_DEFINITIONS 完全對齊）
        Map<String, AspectPref> aspects = new LinkedHashMap<>();
        aspects.put("conjunction",    new AspectPref(toDouble(e.getOrbConjunction()),    e.getShowConjunction()));
        aspects.put("sextile",        new AspectPref(toDouble(e.getOrbSextile()),        e.getShowSextile()));
        aspects.put("square",         new AspectPref(toDouble(e.getOrbSquare()),         e.getShowSquare()));
        aspects.put("trine",          new AspectPref(toDouble(e.getOrbTrine()),          e.getShowTrine()));
        aspects.put("opposition",     new AspectPref(toDouble(e.getOrbOpposition()),     e.getShowOpposition()));
        aspects.put("semiSextile",    new AspectPref(toDouble(e.getOrbSemiSextile()),    e.getShowSemiSextile()));
        aspects.put("semiSquare",     new AspectPref(toDouble(e.getOrbSemiSquare()),     e.getShowSemiSquare()));
        aspects.put("quintile",       new AspectPref(toDouble(e.getOrbQuintile()),       e.getShowQuintile()));
        aspects.put("sesquiquadrate", new AspectPref(toDouble(e.getOrbSesquiquadrate()), e.getShowSesquiquadrate()));
        aspects.put("quincunx",       new AspectPref(toDouble(e.getOrbQuincunx()),       e.getShowQuincunx()));
        dto.setAspects(aspects);

        // strictMode
        dto.setStrictMode(e.getStrictMode());

        // planetOrbs（7 顆主要行星）
        Map<String, Double> orbs = new LinkedHashMap<>();
        orbs.put("sun",     toDouble(e.getOrbSun()));
        orbs.put("moon",    toDouble(e.getOrbMoon()));
        orbs.put("mercury", toDouble(e.getOrbMercury()));
        orbs.put("venus",   toDouble(e.getOrbVenus()));
        orbs.put("mars",    toDouble(e.getOrbMars()));
        orbs.put("jupiter", toDouble(e.getOrbJupiter()));
        orbs.put("saturn",  toDouble(e.getOrbSaturn()));
        dto.setPlanetOrbs(orbs);

        // planetVisibility（外行星 5 + 小天體 13 = 18 個）
        // camelCase key 與前端 DEFAULT_PREFERENCES.planetVisibility 完全對齊
        Map<String, Boolean> vis = new LinkedHashMap<>();
        vis.put("uranus",    e.getShowUranus());
        vis.put("neptune",   e.getShowNeptune());
        vis.put("pluto",     e.getShowPluto());
        vis.put("asc",       e.getShowAsc());
        vis.put("mc",        e.getShowMc());
        vis.put("chiron",    e.getShowChiron());
        vis.put("ceres",     e.getShowCeres());
        vis.put("pallas",    e.getShowPallas());
        vis.put("juno",      e.getShowJuno());
        vis.put("vesta",     e.getShowVesta());
        vis.put("northNode", e.getShowNorthNode());
        vis.put("southNode", e.getShowSouthNode());
        vis.put("lilith",    e.getShowLilith());
        vis.put("pof",       e.getShowPof());
        vis.put("vertex",    e.getShowVertex());
        vis.put("eastPoint", e.getShowEastPoint());
        vis.put("dsc",       e.getShowDsc());
        vis.put("ic",        e.getShowIc());
        dto.setPlanetVisibility(vis);

        return dto;
    }

    // ── helper ──────────────────────────────────────────────────────────
    private static Double toDouble(BigDecimal bd) {
        return bd != null ? bd.doubleValue() : null;
    }
}
