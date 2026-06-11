package com.project.zodiac_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_notes")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class AnalysisNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "client_id", nullable = false)
    private Integer clientId;

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    // ── v17 新增：選填標記欄位 ──────────────────────────
    // 全部 NULL = 未標記，不影響自由書寫
    // 填了之後 AI 可精準對應到 element_notes / transit_notes 的同 key 內容

    // 對應行星（Q=太陽 W=月亮 E=水星 R=金星 T=火星 Y=木星 U=土星 I=天王星 O=海王星 P=冥王星）
    @Column(name = "planet_key", length = 10)
    private String planetKey;

    // 對應星座（a=牡羊 s=金牛 d=雙子 f=巨蟹 g=獅子 h=處女 j=天秤 k=天蠍 l=射手 z=摩羯 x=水瓶 c=雙魚）
    @Column(name = "sign_key", length = 10)
    private String signKey;

    // 對應宮位（1~12）；NULL = 未標記
    @Column(name = "house_key")
    private Short houseKey;

    // 主題分類：general / career / love / wealth / challenge；NULL = 未分類
    @Column(name = "topic", length = 20)
    private String topic;
    // ────────────────────────────────────────────────────

    // 後端新增時自動取目前最大值 + 1；前端 POST 不傳此欄位
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
