package com.project.zodiac_api.service;

import com.project.zodiac_api.dto.ChartPreferencesDTO;
import com.project.zodiac_api.model.ChartPreferences;
import com.project.zodiac_api.repository.ChartPreferencesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ChartPreferences CRUD 服務。
 *
 * 預設值常數（與 schema.sql DEFAULT 值逐一對應，修改時兩邊同步）：
 * ┌─────────────────────────┬──────────┬────────────────────────────────────┐
 * │ DB 欄位                 │ DEFAULT  │ Java 常數                          │
 * ├─────────────────────────┼──────────┼────────────────────────────────────┤
 * │ orb_conjunction         │ 8.0      │ D_ORB_CONJUNCTION                  │
 * │ orb_sextile             │ 6.0      │ D_ORB_SEXTILE                      │
 * │ orb_square              │ 8.0      │ D_ORB_SQUARE                       │
 * │ orb_trine               │ 8.0      │ D_ORB_TRINE                        │
 * │ orb_opposition          │ 8.0      │ D_ORB_OPPOSITION                   │
 * │ orb_semi_sextile        │ 2.0      │ D_ORB_SEMI_SEXTILE                 │
 * │ orb_semi_square         │ 2.0      │ D_ORB_SEMI_SQUARE                  │
 * │ orb_quintile            │ 2.0      │ D_ORB_QUINTILE                     │
 * │ orb_sesquiquadrate      │ 2.0      │ D_ORB_SESQUIQUADRATE               │
 * │ orb_quincunx            │ 3.0      │ D_ORB_QUINCUNX                     │
 * │ show_conjunction        │ TRUE     │ (hardcoded true)                   │
 * │ show_sextile            │ TRUE     │                                    │
 * │ show_square             │ TRUE     │                                    │
 * │ show_trine              │ TRUE     │                                    │
 * │ show_opposition         │ TRUE     │                                    │
 * │ show_semi_sextile       │ FALSE    │ (hardcoded false)                  │
 * │ show_semi_square        │ FALSE    │                                    │
 * │ show_quintile           │ FALSE    │                                    │
 * │ show_sesquiquadrate     │ FALSE    │                                    │
 * │ show_quincunx           │ FALSE    │                                    │
 * │ strict_mode             │ FALSE    │                                    │
 * │ orb_sun                 │ 8.0      │ D_ORB_SUN                          │
 * │ orb_moon                │ 8.0      │ D_ORB_MOON                         │
 * │ orb_mercury             │ 6.0      │ D_ORB_MERCURY                      │
 * │ orb_venus               │ 6.0      │ D_ORB_VENUS                        │
 * │ orb_mars                │ 6.0      │ D_ORB_MARS                         │
 * │ orb_jupiter             │ 5.0      │ D_ORB_JUPITER                      │
 * │ orb_saturn              │ 5.0      │ D_ORB_SATURN                       │
 * │ show_uranus             │ TRUE     │                                    │
 * │ show_neptune            │ TRUE     │                                    │
 * │ show_pluto              │ TRUE     │                                    │
 * │ show_asc                │ TRUE     │                                    │
 * │ show_mc                 │ TRUE     │                                    │
 * │ show_chiron             │ FALSE    │ (hardcoded false)                  │
 * │ show_ceres              │ FALSE    │                                    │
 * │ show_pallas             │ FALSE    │                                    │
 * │ show_juno               │ FALSE    │                                    │
 * │ show_vesta              │ FALSE    │                                    │
 * │ show_north_node         │ FALSE    │                                    │
 * │ show_south_node         │ FALSE    │                                    │
 * │ show_lilith             │ FALSE    │                                    │
 * │ show_pof                │ FALSE    │                                    │
 * │ show_vertex             │ FALSE    │                                    │
 * │ show_east_point         │ FALSE    │                                    │
 * │ show_dsc                │ FALSE    │                                    │
 * │ show_ic                 │ FALSE    │                                    │
 * └─────────────────────────┴──────────┴────────────────────────────────────┘
 *
 * TODO（待開發）：將上述預設值改為從 DB 設定表（chart_preference_defaults）讀取，
 *                 並在前端提供編輯介面，避免修改預設值時需改 Java 程式碼。
 *                 詳見規格書「待辦開發清單 — 設定檔功能（V3）」。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChartPreferencesService {

    private final ChartPreferencesRepository prefsRepo;

    // ── 預設值常數（與 schema.sql DEFAULT 完全一致，兩邊修改請同步）──────
    private static final BigDecimal D_ORB_CONJUNCTION    = new BigDecimal("8.0");
    private static final BigDecimal D_ORB_SEXTILE        = new BigDecimal("6.0");
    private static final BigDecimal D_ORB_SQUARE         = new BigDecimal("8.0");
    private static final BigDecimal D_ORB_TRINE          = new BigDecimal("8.0");
    private static final BigDecimal D_ORB_OPPOSITION     = new BigDecimal("8.0");
    private static final BigDecimal D_ORB_SEMI_SEXTILE   = new BigDecimal("2.0");
    private static final BigDecimal D_ORB_SEMI_SQUARE    = new BigDecimal("2.0");
    private static final BigDecimal D_ORB_QUINTILE       = new BigDecimal("2.0");
    private static final BigDecimal D_ORB_SESQUIQUADRATE = new BigDecimal("2.0");
    private static final BigDecimal D_ORB_QUINCUNX       = new BigDecimal("3.0");
    private static final BigDecimal D_ORB_SUN            = new BigDecimal("8.0");
    private static final BigDecimal D_ORB_MOON           = new BigDecimal("8.0");
    private static final BigDecimal D_ORB_MERCURY        = new BigDecimal("6.0");
    private static final BigDecimal D_ORB_VENUS          = new BigDecimal("6.0");
    private static final BigDecimal D_ORB_MARS           = new BigDecimal("6.0");
    private static final BigDecimal D_ORB_JUPITER        = new BigDecimal("5.0");
    private static final BigDecimal D_ORB_SATURN         = new BigDecimal("5.0");

    // ─────────────────────────────────────────────────────────────────────
    // GET — 取得目前設定
    // 若 id=1 不存在（edge case：DB 被手動清除），回傳 hardcoded 預設 DTO，
    // 不自動寫入 DB（避免 write in read path；save/reset 則 orElseThrow）。
    // ─────────────────────────────────────────────────────────────────────
    public ChartPreferencesDTO get() {
        return prefsRepo.findById(1)
                .map(ChartPreferencesDTO::from)
                .orElseGet(() -> {
                    log.warn("[ChartPreferences] id=1 不存在，回傳 hardcoded 預設值（不寫入 DB）");
                    return buildDefaultDTO();
                });
    }

    // ─────────────────────────────────────────────────────────────────────
    // PUT — 儲存設定
    // 將前端巢狀 DTO 拆解，逐欄寫入 Entity。
    // @PreUpdate 自動更新 updated_at。
    // ─────────────────────────────────────────────────────────────────────
    @Transactional
    public ChartPreferencesDTO save(ChartPreferencesDTO dto) {
        ChartPreferences entity = prefsRepo.findById(1)
                .orElseThrow(() -> new IllegalStateException(
                        "[ChartPreferences] id=1 不存在，請重新執行 schema.sql 初始化資料庫"));
        applyDto(dto, entity);
        return ChartPreferencesDTO.from(prefsRepo.save(entity));
    }

    // ─────────────────────────────────────────────────────────────────────
    // POST reset — 還原所有欄位為 schema 預設值
    // applyDefaults() 可能將值設回原本就相同的數字，JPA 可能偵測無 dirty 欄位而跳過 UPDATE。
    // 因此明確呼叫 entity.setUpdatedAt()，確保至少一個欄位 dirty → 觸發 SQL UPDATE。
    // @PreUpdate 還是會再次設定 updatedAt（兩次設定同一個欄位，harmless）。
    // ─────────────────────────────────────────────────────────────────────
    @Transactional
    public ChartPreferencesDTO reset() {
        ChartPreferences entity = prefsRepo.findById(1)
                .orElseThrow(() -> new IllegalStateException(
                        "[ChartPreferences] id=1 不存在，請重新執行 schema.sql 初始化資料庫"));
        applyDefaults(entity);
        entity.setUpdatedAt(LocalDateTime.now()); // 確保 dirty，即使所有值已是預設也會觸發 UPDATE
        return ChartPreferencesDTO.from(prefsRepo.save(entity));
    }

    // ─────────────────────────────────────────────────────────────────────
    // 內部：DTO → Entity mapping（巢狀結構拆解為 48 個扁平欄位）
    // null 值不寫入（允許 partial update，防禦性程式設計）
    // ─────────────────────────────────────────────────────────────────────
    private void applyDto(ChartPreferencesDTO dto, ChartPreferences e) {

        // ── 相位容許度 + 顯示開關 ────────────────────────────────────
        if (dto.getAspects() != null) {
            ChartPreferencesDTO.AspectPref p;

            p = dto.getAspects().get("conjunction");
            if (p != null) {
                if (p.getOrb()  != null) e.setOrbConjunction(toBD(p.getOrb()));
                if (p.getShow() != null) e.setShowConjunction(p.getShow());
            }
            p = dto.getAspects().get("sextile");
            if (p != null) {
                if (p.getOrb()  != null) e.setOrbSextile(toBD(p.getOrb()));
                if (p.getShow() != null) e.setShowSextile(p.getShow());
            }
            p = dto.getAspects().get("square");
            if (p != null) {
                if (p.getOrb()  != null) e.setOrbSquare(toBD(p.getOrb()));
                if (p.getShow() != null) e.setShowSquare(p.getShow());
            }
            p = dto.getAspects().get("trine");
            if (p != null) {
                if (p.getOrb()  != null) e.setOrbTrine(toBD(p.getOrb()));
                if (p.getShow() != null) e.setShowTrine(p.getShow());
            }
            p = dto.getAspects().get("opposition");
            if (p != null) {
                if (p.getOrb()  != null) e.setOrbOpposition(toBD(p.getOrb()));
                if (p.getShow() != null) e.setShowOpposition(p.getShow());
            }
            p = dto.getAspects().get("semiSextile");
            if (p != null) {
                if (p.getOrb()  != null) e.setOrbSemiSextile(toBD(p.getOrb()));
                if (p.getShow() != null) e.setShowSemiSextile(p.getShow());
            }
            p = dto.getAspects().get("semiSquare");
            if (p != null) {
                if (p.getOrb()  != null) e.setOrbSemiSquare(toBD(p.getOrb()));
                if (p.getShow() != null) e.setShowSemiSquare(p.getShow());
            }
            p = dto.getAspects().get("quintile");
            if (p != null) {
                if (p.getOrb()  != null) e.setOrbQuintile(toBD(p.getOrb()));
                if (p.getShow() != null) e.setShowQuintile(p.getShow());
            }
            p = dto.getAspects().get("sesquiquadrate");
            if (p != null) {
                if (p.getOrb()  != null) e.setOrbSesquiquadrate(toBD(p.getOrb()));
                if (p.getShow() != null) e.setShowSesquiquadrate(p.getShow());
            }
            p = dto.getAspects().get("quincunx");
            if (p != null) {
                if (p.getOrb()  != null) e.setOrbQuincunx(toBD(p.getOrb()));
                if (p.getShow() != null) e.setShowQuincunx(p.getShow());
            }
        }

        // ── 嚴謹模式 ─────────────────────────────────────────────────
        if (dto.getStrictMode() != null) {
            e.setStrictMode(dto.getStrictMode());
        }

        // ── 行星個別容許度 ────────────────────────────────────────────
        if (dto.getPlanetOrbs() != null) {
            var orbs = dto.getPlanetOrbs();
            if (orbs.get("sun")     != null) e.setOrbSun(toBD(orbs.get("sun")));
            if (orbs.get("moon")    != null) e.setOrbMoon(toBD(orbs.get("moon")));
            if (orbs.get("mercury") != null) e.setOrbMercury(toBD(orbs.get("mercury")));
            if (orbs.get("venus")   != null) e.setOrbVenus(toBD(orbs.get("venus")));
            if (orbs.get("mars")    != null) e.setOrbMars(toBD(orbs.get("mars")));
            if (orbs.get("jupiter") != null) e.setOrbJupiter(toBD(orbs.get("jupiter")));
            if (orbs.get("saturn")  != null) e.setOrbSaturn(toBD(orbs.get("saturn")));
        }

        // ── 行星 / 小天體顯示開關 ─────────────────────────────────────
        if (dto.getPlanetVisibility() != null) {
            var vis = dto.getPlanetVisibility();
            if (vis.get("uranus")    != null) e.setShowUranus(vis.get("uranus"));
            if (vis.get("neptune")   != null) e.setShowNeptune(vis.get("neptune"));
            if (vis.get("pluto")     != null) e.setShowPluto(vis.get("pluto"));
            if (vis.get("asc")       != null) e.setShowAsc(vis.get("asc"));
            if (vis.get("mc")        != null) e.setShowMc(vis.get("mc"));
            if (vis.get("chiron")    != null) e.setShowChiron(vis.get("chiron"));
            if (vis.get("ceres")     != null) e.setShowCeres(vis.get("ceres"));
            if (vis.get("pallas")    != null) e.setShowPallas(vis.get("pallas"));
            if (vis.get("juno")      != null) e.setShowJuno(vis.get("juno"));
            if (vis.get("vesta")     != null) e.setShowVesta(vis.get("vesta"));
            if (vis.get("northNode") != null) e.setShowNorthNode(vis.get("northNode"));
            if (vis.get("southNode") != null) e.setShowSouthNode(vis.get("southNode"));
            if (vis.get("lilith")    != null) e.setShowLilith(vis.get("lilith"));
            if (vis.get("pof")       != null) e.setShowPof(vis.get("pof"));
            if (vis.get("vertex")    != null) e.setShowVertex(vis.get("vertex"));
            if (vis.get("eastPoint") != null) e.setShowEastPoint(vis.get("eastPoint"));
            if (vis.get("dsc")       != null) e.setShowDsc(vis.get("dsc"));
            if (vis.get("ic")        != null) e.setShowIc(vis.get("ic"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 內部：將所有欄位設回 schema 預設值
    // ─────────────────────────────────────────────────────────────────────
    private void applyDefaults(ChartPreferences e) {
        // 相位容許度
        e.setOrbConjunction(D_ORB_CONJUNCTION);
        e.setOrbSextile(D_ORB_SEXTILE);
        e.setOrbSquare(D_ORB_SQUARE);
        e.setOrbTrine(D_ORB_TRINE);
        e.setOrbOpposition(D_ORB_OPPOSITION);
        e.setOrbSemiSextile(D_ORB_SEMI_SEXTILE);
        e.setOrbSemiSquare(D_ORB_SEMI_SQUARE);
        e.setOrbQuintile(D_ORB_QUINTILE);
        e.setOrbSesquiquadrate(D_ORB_SESQUIQUADRATE);
        e.setOrbQuincunx(D_ORB_QUINCUNX);

        // 相位顯示（主要 5 個 TRUE，次要 5 個 FALSE）
        e.setShowConjunction(true);
        e.setShowSextile(true);
        e.setShowSquare(true);
        e.setShowTrine(true);
        e.setShowOpposition(true);
        e.setShowSemiSextile(false);
        e.setShowSemiSquare(false);
        e.setShowQuintile(false);
        e.setShowSesquiquadrate(false);
        e.setShowQuincunx(false);

        // 嚴謹模式
        e.setStrictMode(false);

        // 行星個別容許度
        e.setOrbSun(D_ORB_SUN);
        e.setOrbMoon(D_ORB_MOON);
        e.setOrbMercury(D_ORB_MERCURY);
        e.setOrbVenus(D_ORB_VENUS);
        e.setOrbMars(D_ORB_MARS);
        e.setOrbJupiter(D_ORB_JUPITER);
        e.setOrbSaturn(D_ORB_SATURN);

        // 外行星 / 軸點（預設開）
        e.setShowUranus(true);
        e.setShowNeptune(true);
        e.setShowPluto(true);
        e.setShowAsc(true);
        e.setShowMc(true);

        // 小天體（預設全關，共 13 個）
        e.setShowChiron(false);
        e.setShowCeres(false);
        e.setShowPallas(false);
        e.setShowJuno(false);
        e.setShowVesta(false);
        e.setShowNorthNode(false);
        e.setShowSouthNode(false);
        e.setShowLilith(false);
        e.setShowPof(false);
        e.setShowVertex(false);
        e.setShowEastPoint(false);
        e.setShowDsc(false);
        e.setShowIc(false);
    }

    // ─────────────────────────────────────────────────────────────────────
    // 內部：建立預設值 DTO（不觸碰 DB，供 get() 的 orElseGet fallback）
    // ─────────────────────────────────────────────────────────────────────
    private ChartPreferencesDTO buildDefaultDTO() {
        ChartPreferences temp = new ChartPreferences();
        applyDefaults(temp);
        return ChartPreferencesDTO.from(temp);
    }

    // ─────────────────────────────────────────────────────────────────────
    // helper
    // ─────────────────────────────────────────────────────────────────────
    private static BigDecimal toBD(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }
}
