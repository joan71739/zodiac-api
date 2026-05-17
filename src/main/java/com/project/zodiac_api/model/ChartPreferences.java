package com.project.zodiac_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * chart_preferences 對應 Entity。
 * 設計原則：全域單筆，id 永遠為 1。
 * 系統啟動時由 schema.sql 的 INSERT INTO chart_preferences DEFAULT VALUES 建立。
 * JPA 只執行 SELECT / UPDATE，從不 INSERT（無 @GeneratedValue）。
 * @PreUpdate 在 save() 時自動更新 updated_at。
 */
@Entity
@Table(name = "chart_preferences")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ChartPreferences {

    /** 永遠為 1，由 SQL 初始化，JPA 不自動生成 */
    @Id
    private Integer id;

    // ── 相位容許度（單位：度，DECIMAL(4,1)）──────────────────────────
    @Column(name = "orb_conjunction",    precision = 4, scale = 1)
    private BigDecimal orbConjunction;

    @Column(name = "orb_sextile",        precision = 4, scale = 1)
    private BigDecimal orbSextile;

    @Column(name = "orb_square",         precision = 4, scale = 1)
    private BigDecimal orbSquare;

    @Column(name = "orb_trine",          precision = 4, scale = 1)
    private BigDecimal orbTrine;

    @Column(name = "orb_opposition",     precision = 4, scale = 1)
    private BigDecimal orbOpposition;

    @Column(name = "orb_semi_sextile",   precision = 4, scale = 1)
    private BigDecimal orbSemiSextile;

    @Column(name = "orb_semi_square",    precision = 4, scale = 1)
    private BigDecimal orbSemiSquare;

    @Column(name = "orb_quintile",       precision = 4, scale = 1)
    private BigDecimal orbQuintile;

    @Column(name = "orb_sesquiquadrate", precision = 4, scale = 1)
    private BigDecimal orbSesquiquadrate;

    @Column(name = "orb_quincunx",       precision = 4, scale = 1)
    private BigDecimal orbQuincunx;

    // ── 相位顯示開關 ──────────────────────────────────────────────────
    @Column(name = "show_conjunction")
    private Boolean showConjunction;

    @Column(name = "show_sextile")
    private Boolean showSextile;

    @Column(name = "show_square")
    private Boolean showSquare;

    @Column(name = "show_trine")
    private Boolean showTrine;

    @Column(name = "show_opposition")
    private Boolean showOpposition;

    @Column(name = "show_semi_sextile")
    private Boolean showSemiSextile;

    @Column(name = "show_semi_square")
    private Boolean showSemiSquare;

    @Column(name = "show_quintile")
    private Boolean showQuintile;

    @Column(name = "show_sesquiquadrate")
    private Boolean showSesquiquadrate;

    @Column(name = "show_quincunx")
    private Boolean showQuincunx;

    // ── 嚴謹模式 ──────────────────────────────────────────────────────
    @Column(name = "strict_mode")
    private Boolean strictMode;

    // ── 行星個別容許度（僅日月水金火木土）────────────────────────────
    @Column(name = "orb_sun",     precision = 4, scale = 1)
    private BigDecimal orbSun;

    @Column(name = "orb_moon",    precision = 4, scale = 1)
    private BigDecimal orbMoon;

    @Column(name = "orb_mercury", precision = 4, scale = 1)
    private BigDecimal orbMercury;

    @Column(name = "orb_venus",   precision = 4, scale = 1)
    private BigDecimal orbVenus;

    @Column(name = "orb_mars",    precision = 4, scale = 1)
    private BigDecimal orbMars;

    @Column(name = "orb_jupiter", precision = 4, scale = 1)
    private BigDecimal orbJupiter;

    @Column(name = "orb_saturn",  precision = 4, scale = 1)
    private BigDecimal orbSaturn;

    // ── 外行星 / 軸點顯示開關 ────────────────────────────────────────
    @Column(name = "show_uranus")
    private Boolean showUranus;

    @Column(name = "show_neptune")
    private Boolean showNeptune;

    @Column(name = "show_pluto")
    private Boolean showPluto;

    @Column(name = "show_asc")
    private Boolean showAsc;

    @Column(name = "show_mc")
    private Boolean showMc;

    // ── 小天體顯示開關（共 13 個）────────────────────────────────────
    @Column(name = "show_chiron")
    private Boolean showChiron;      // 凱龍星

    @Column(name = "show_ceres")
    private Boolean showCeres;       // 穀神星

    @Column(name = "show_pallas")
    private Boolean showPallas;      // 智神星

    @Column(name = "show_juno")
    private Boolean showJuno;        // 婚神星

    @Column(name = "show_vesta")
    private Boolean showVesta;       // 灶神星

    @Column(name = "show_north_node")
    private Boolean showNorthNode;   // 北交點

    @Column(name = "show_south_node")
    private Boolean showSouthNode;   // 南交點

    @Column(name = "show_lilith")
    private Boolean showLilith;      // 莉莉絲（黑月）

    @Column(name = "show_pof")
    private Boolean showPof;         // 幸運點（Part of Fortune）

    @Column(name = "show_vertex")
    private Boolean showVertex;      // 宿命點

    @Column(name = "show_east_point")
    private Boolean showEastPoint;   // 東昇點

    @Column(name = "show_dsc")
    private Boolean showDsc;         // 下降點（備用）

    @Column(name = "show_ic")
    private Boolean showIc;          // 天底（備用）

    // ── 時間戳記 ──────────────────────────────────────────────────────
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * JPA save() 時自動更新 updated_at。
     * 注意：若用 JPQL bulk update 語句，此 callback 不會觸發。
     * 本專案只用 findById(1) → 修改欄位 → save()，因此此處可正常觸發。
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
