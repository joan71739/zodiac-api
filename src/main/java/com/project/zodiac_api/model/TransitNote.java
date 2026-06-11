package com.project.zodiac_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "transit_notes")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class TransitNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 行運行星（必填）：Y=木星 U=土星 I=天王星 O=海王星 P=冥王星
    @Column(name = "transit_planet", nullable = false, length = 10)
    private String transitPlanet;

    // 相位類型：NULL = 過境宮位情境
    // q=合相 w=對分相 e=三分相 r=四分相 t=六分相
    @Column(name = "aspect_type", length = 10)
    private String aspectType;

    // 本命星：NULL = 過境宮位情境
    // Q=太陽 W=月亮 E=水星 R=金星 T=火星
    @Column(name = "natal_planet", length = 10)
    private String natalPlanet;

    // 行運行星過境的宮位（1~12）：NULL = 行運星×相位×本命星情境
    @Column(name = "transit_house")
    private Short transitHouse;

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    // 標籤（手動輸入）
    @Column(length = 200)
    private String tag;

    // 主題分類：general / career / love / wealth / challenge；NULL = 未分類
    @Column(length = 20)
    private String topic;

    // 數字越大越新，最新在最上
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
